package com.example.fithub

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.example.fithub.data.ExerciseDto
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.fithub.data.AddMealDto
import com.example.fithub.data.CreateFoodDto
import com.example.fithub.data.DailyNutritionDto
import com.example.fithub.data.FoodItemDto
import com.example.fithub.data.MealDto
import com.example.fithub.data.NutritionData
import com.example.fithub.data.PointsManager
import com.example.fithub.logic.UserCalculator
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
        return NetworkModule.api.getExercisesByName(query)
    }

    override fun createResultView(item: ExerciseDto): View {
        val view = LayoutInflater.from(requireContext())
            .inflate(android.R.layout.simple_list_item_2, llSearchResults, false)

        view.findViewById<TextView>(android.R.id.text1).text = item.name ?: "Bez nazwy"

        val muscleInfo = item.muscleIds?.joinToString(", ") ?: "Brak danych"
        val metsInfo = item.mets?.let { "METS: $it" } ?: ""
        view.findViewById<TextView>(android.R.id.text2).text = "$muscleInfo${if (metsInfo.isNotEmpty()) " • $metsInfo" else ""}"

        return view
    }

    override fun onItemSelected(item: ExerciseDto) {
        showSetDetailsDialog(item)
    }

    private fun showSetDetailsDialog(exercise: ExerciseDto) {
        val dialogLayout = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }


        val etDuration = android.widget.EditText(requireContext()).apply {
            hint = "Czas trwania (min)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }


        dialogLayout.addView(etDuration)

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Szczegóły: ${exercise.name ?: "Ćwiczenie"}")
            .setView(dialogLayout)
            .setPositiveButton("Dodaj") { _, _ ->
                val duration = etDuration.text.toString().toDouble()

                if (duration <= 0) {
                    Toast.makeText(requireContext(), "Podaj czas trwania", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                saveExercise(exercise, duration)
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun saveExercise(exercise: ExerciseDto, duration: Double){
        val userId = arguments?.getString("userId") ?: return
        val date = arguments?.getString("date") ?: return

        lifecycleScope.launch {
            try {

                val user = NetworkModule.api.getUserById(userId)
                val userWeight = user.profile.weightKg.toDouble()
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
                    brand = "Trening",
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
                    addedBy = userId
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
                    userId = userId,
                    date = date,
                    addMealDto = payload
                )

                try {
                    Log.d("AddMealDialog", "Posiłek dodany, przyznaję punkty...")
                    val leveledUp = PointsManager.addPoints(userId, PointsManager.ActionType.TRAINING)

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