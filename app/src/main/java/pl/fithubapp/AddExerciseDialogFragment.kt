package pl.fithubapp

import android.app.AlertDialog
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import pl.fithubapp.data.ExerciseDto
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import pl.fithubapp.data.ChallengeManager
import pl.fithubapp.data.AddMealDto
import pl.fithubapp.data.ChallengeType
import pl.fithubapp.data.CreateFoodDto
import pl.fithubapp.data.FoodItemDto
import pl.fithubapp.data.MealDto
import pl.fithubapp.data.NutritionData
import pl.fithubapp.data.PointsManager
import pl.fithubapp.logic.UserCalculator
import kotlinx.coroutines.launch
import kotlin.Double

class AddExerciseDialogFragment : SearchDialogFragment<ExerciseDto>() {

    interface OnExerciseAddedListener {
        fun onExerciseAdded()
    }

    var onExerciseAddedListener: OnExerciseAddedListener? = null

    override fun getTitle(): String = "Dodaj ćwiczenie"

    override fun getSearchHint(): String = "Wyszukaj ćwiczenie.."

    override suspend fun performSearch(query: String): List<ExerciseDto> {
        val exercises = NetworkModule.api.getExercisesByName(query)
        return exercises.distinctBy {
            it.name?.trim()?.lowercase()
        }
    }

    override fun createResultView(item: ExerciseDto): View {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_exercise_search_result, llSearchResults, false)

        val tvExerciseName = view.findViewById<TextView>(R.id.tvExerciseName)
        val tvExerciseInfo = view.findViewById<TextView>(R.id.tvExerciseInfo)
        
        tvExerciseName.text = item.name ?: "Bez nazwy"

        val muscleInfo = item.muscleIds
            ?.takeIf { it.isNotEmpty() }
            ?.joinToString(", ") { muscleId ->
                MuscleTranslator.translate(muscleId)
            }

        val metsInfo = item.mets?.let { "METS: $it" } ?: ""

        val finalText = buildString {
            if (!muscleInfo.isNullOrEmpty()) {
                append(muscleInfo)
            }
            if (metsInfo.isNotEmpty()) {
                if (!muscleInfo.isNullOrEmpty()) {
                    append(" • ")
                }
                append(metsInfo)
            }
        }

        tvExerciseInfo.text = finalText

        return view
    }

    override fun onItemSelected(item: ExerciseDto) {
        showSetDetailsDialog(item)
    }

    private fun showSetDetailsDialog(exercise: ExerciseDto) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_duration_input, null)
        
        val tilDuration = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilDuration)
        val etDuration = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDuration)

        val dialog = AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_Fithub_Dialog)
            .setTitle("Szczegóły: ${exercise.name ?: "Ćwiczenie"}")
            .setView(dialogView)
            .setPositiveButton("Dodaj", null)
            .setNegativeButton("Anuluj", null)
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val durationText = etDuration.text.toString()
                val duration = durationText.toDoubleOrNull()

                if (duration == null || duration <= 0) {
                    tilDuration.error = "Podaj poprawną liczbę minut"
                    Toast.makeText(requireContext(), "Podaj czas trwania (np. 30)", Toast.LENGTH_SHORT).show()
                } else {
                    saveExercise(exercise, duration)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()

        etDuration.requestFocus()
    }

    private fun saveExercise(exercise: ExerciseDto, duration: Double){
        val date = arguments?.getString("date") ?: return

        lifecycleScope.launch {
            try {

                val user = NetworkModule.api.getCurrentUser()
                val userWeight = user.profile.weightKg
                val mets = exercise.mets ?: run{
                    Log.d("brak mets", "Brak danych mets w bazie")
                    return@launch
                }

                val calculator = UserCalculator()

                val caloriesBurned = calculator.calculateEnergyExpenditure(
                    weight = userWeight,
                    mets = mets,
                    minutes = duration
                ) ?: run {
                    Toast.makeText(requireContext(), "Błąd obliczania spalonych kalorii", Toast.LENGTH_SHORT).show()
                    Log.e("AddExercise", "Błąd obliczania spalonych kalorii")
                    return@launch
                }

                val exerciseName = "${exercise.name} (${duration} min)"

                val exerciseAsFood = CreateFoodDto(
                    name = exerciseName,
                    brand = "SingleExercise",
                    barcode = null,
                    nutritionPer100g = NutritionData(
                        calories = -caloriesBurned,
                        protein = 0.0,
                        fat = 0.0,
                        carbs = 0.0,
                        fiber = 0.0,
                        sugar = 0.0,
                        sodium = 0.0
                    ),
                    category = "Exercise",
                    addedBy = user.id
                )

                val createdExerciseFood = NetworkModule.api.createFood(exerciseAsFood)

                val mealDto = MealDto(
                    name = "Trening",
                    foods = listOf(
                        FoodItemDto(
                            foodId = createdExerciseFood.id,
                            quantity = 100.0
                        )
                    )
                )

                val payload = AddMealDto(meal = mealDto)

                NetworkModule.api.addMeal(
                    date = date,
                    addMealDto = payload
                )

                try {
                    Log.d("AddMealDialog", "Posiłek dodany, przyznaję punkty...")
                    val leveledUp = PointsManager.addPoints(PointsManager.ActionType.TRAINING)

                    ChallengeManager.checkChallengeProgress(ChallengeType.TRAINING_COUNT, 1.0)

                    if (leveledUp) {
                        (activity as? UserMainActivity)?.showLevelUpDialog()
                    }                } catch (e: Exception) {
                    Log.e("AddMealDialog", "Nie udało się dodać punktów: ${e.message}")
                }

                Toast.makeText(
                    requireContext(),
                    "Dodano: ${exercise.name}\nSpalono: ${caloriesBurned.toInt()} kcal",
                    Toast.LENGTH_LONG
                ).show()

                onExerciseAddedListener?.onExerciseAdded()
                dismiss()

            }
            catch (e: Exception){
                Log.e("AddExercise", "Error", e)
                Toast.makeText(
                    requireContext(),
                    "Błąd: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }



}