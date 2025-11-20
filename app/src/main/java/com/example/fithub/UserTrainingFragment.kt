package com.example.fithub

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fithub.data.AddMealDto
import com.example.fithub.data.CreateFoodDto
import com.example.fithub.data.ExerciseDto
import com.example.fithub.data.FoodItemDto
import com.example.fithub.data.MealDto
import com.example.fithub.data.NutritionData
import com.example.fithub.logic.UserCalculator
import kotlinx.coroutines.launch

class UserTrainingFragment : Fragment(R.layout.fragment_user_training) {

    private lateinit var btnShowMuscleModel: Button
    private lateinit var btnSearchExercise: Button
    private lateinit var rvUserExercises: RecyclerView
    private lateinit var exerciseAdapter: ExerciseListAdapter
    private lateinit var exercisePlanName: TextView
    private lateinit var cbPlanCompleted: CheckBox


    val currentUserId = "68cbc06e6cdfa7faa8561f82"
    private var currentPlanName = "Plan treningowy 1"
    private var currentPlanId = ""
    private var isUserInitiatedChange = true

    private val exercisesInPlan = mutableListOf<ExerciseListAdapter.ExerciseItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnShowMuscleModel = view.findViewById(R.id.btnShowMuscleModel)
        btnSearchExercise = view.findViewById(R.id.btnSearchExercise)
        rvUserExercises = view.findViewById(R.id.rvUserExercises)
        exercisePlanName = view.findViewById(R.id.tvExercisePlanName)
        cbPlanCompleted = view.findViewById(R.id.cbPlanCompleted)

        setupRecyclerView()

        btnShowMuscleModel.setOnClickListener {
            openMuscleModel()
        }

        btnSearchExercise.setOnClickListener {
            openAddExerciseToPlanDialog()
        }

        exercisePlanName.setOnClickListener {
            val dialog = SelectExercisePlanDialogFragment.newInstance(currentUserId)

            dialog.onPlanSelectedListener = object : SelectExercisePlanDialogFragment.OnPlanSelectedListener {
                override fun onPlanSelected(planId: String, planName: String) {
                    currentPlanId = planId
                    currentPlanName = planName
                    exercisePlanName.text = planName
                    Toast.makeText(requireContext(), "Wybrano: $planName", Toast.LENGTH_SHORT).show()
                    loadExercisesForCurrentPlan()
                }
            }
            dialog.show(parentFragmentManager, "SelectPlanDialog")
        }

        cbPlanCompleted.setOnCheckedChangeListener {
            _, isChecked ->
            if (isUserInitiatedChange && isChecked){
                showFinishTrainingDialog()
            }
        }

        loadExercisesForCurrentPlan()
    }

    private fun setupRecyclerView() {
        exerciseAdapter = ExerciseListAdapter(
            onExerciseClick = { exercise ->
                Toast.makeText(requireContext(), "Kliknięto: ${exercise.name}", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { exercise ->
                deleteExerciseFromPlan(exercise)
            },
            showDeleteButton = true,
            lifecycle = viewLifecycleOwner.lifecycle
        )

        rvUserExercises.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = exerciseAdapter
        }
    }

    private fun loadExercisesForCurrentPlan() {
        lifecycleScope.launch {
            try {
                // Pobierz wszystkie plany użytkownika
                val plans = NetworkModule.api.getUserExercisePlans(currentUserId)
                Log.d("UserTraining", "Pobrano ${plans.size} planów")

                // Znajdź plan o nazwie currentPlanName
                val currentPlan = plans.find { it.planName == currentPlanName }

                if (currentPlan != null) {
                    currentPlanId = currentPlan.id

                    // Pobierz ćwiczenia z planu
                    val exercises = currentPlan.planExercises.map { planExercise ->
                        ExerciseListAdapter.ExerciseItem(
                            exercise = planExercise.exerciseId
                        )
                    }

                    Log.d("UserTraining", "Znaleziono ${exercises.size} ćwiczeń w planie: $currentPlanName")
                    exerciseAdapter.submitList(exercises)

                } else {
                    Log.w("UserTraining", "Nie znaleziono planu: $currentPlanName")
                    exerciseAdapter.submitList(emptyList())
                }

            } catch (e: Exception) {
                Log.e("UserTraining", "Błąd ładowania ćwiczeń", e)
                Toast.makeText(requireContext(), "Błąd: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteExerciseFromPlan(exercise: ExerciseDto) {
        lifecycleScope.launch {
            try {
                // Usuń ćwiczenie z planu używając istniejącego endpointu
                NetworkModule.api.removeExerciseFromPlan(currentPlanId, exercise.id)

                Toast.makeText(requireContext(), "Usunięto: ${exercise.name}", Toast.LENGTH_SHORT).show()

                // Odśwież listę
                loadExercisesForCurrentPlan()

            } catch (e: Exception) {
                Log.e("UserTraining", "Błąd usuwania ćwiczenia", e)
                Toast.makeText(requireContext(), "Błąd: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openAddExerciseToPlanDialog(){
        val dialog = AddExerciseToPlanDialogFragment().apply {
            arguments = Bundle().apply {
                putString("userId", currentUserId)
                putString("planName", currentPlanName)
            }
        }

        dialog.onExerciseAddedToPlanListener = object: AddExerciseToPlanDialogFragment.OnExerciseAddedToPlanListener{
            override fun onExerciseAddedToPlan() {
                loadExercisesForCurrentPlan()
            }
        }

        dialog.show(parentFragmentManager, "AddExerciseToPlanDialog")
    }

    private fun openMuscleModel() {
        parentFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.main_container, MuscleModelFragment.newInstance(currentUserId, currentPlanName))
            addToBackStack("MuscleModel")
        }
    }

    fun onMuscleClicked(muscleId: String) {
        Toast.makeText(requireContext(), "Wybrano: $muscleId", Toast.LENGTH_SHORT).show()
    }

    private fun showFinishTrainingDialog() {
        val inputLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val input = EditText(requireContext()).apply {
            hint = "Czas trwania (minuty)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText("60")
        }

        inputLayout.addView(input)

        AlertDialog.Builder(requireContext())
            .setTitle("Zakończono trening")
            .setMessage("Czy dodać ten trening do dziennika kalorii? Podaj czas trwania (w minutach):")
            .setView(inputLayout)
            .setPositiveButton("Dodaj") { _, _ ->
                val minutes = input.text.toString().toDoubleOrNull()
                if (minutes != null && minutes > 0) {
                    addTrainingSummaryToDiary(minutes)
                } else {
                    Toast.makeText(requireContext(), "Podaj poprawny czas", Toast.LENGTH_SHORT).show()
                    // Cofamy zaznaczenie jeśli błąd
                    isUserInitiatedChange = false
                    cbPlanCompleted.isChecked = false
                    isUserInitiatedChange = true
                }
            }
            .setNegativeButton("Anuluj") { _, _ ->
            }
            .setOnCancelListener {
                isUserInitiatedChange = false
                cbPlanCompleted.isChecked = false
                isUserInitiatedChange = true
            }
            .show()
    }

    private fun addTrainingSummaryToDiary(minutes: Double){
        lifecycleScope.launch {
            try {
                val user = NetworkModule.api.getUserById(currentUserId)
                val weight = user.profile.weightKg.toDouble()

                val averageMets = 5.0

                val calculator = UserCalculator()
                val caloriesBurned =  calculator.calculateEnergyExpenditure(weight,averageMets, minutes) ?: 0.0

                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val dateStr = dateFormat.format(java.util.Date())

                val trainingFood = CreateFoodDto(
                    name = "Wykonany plan: $currentPlanName",
                    brand = "Trening",
                    barcode = null,
                    nutritionPer100g = NutritionData(
                        calories = -caloriesBurned,
                        protein = 0.0, fat = 0.0, carbs = 0.0, fiber = 0.0, sugar = 0.0, sodium = 0.0
                    ),
                    category = "Exercise",
                    addedBy = currentUserId
                )

                val createdFood = NetworkModule.api.createFood(trainingFood)

                val mealDto = MealDto(
                    name = "Trening",
                    foods = listOf(
                        FoodItemDto(
                            foodId = createdFood.id,
                            quantity = 100.0
                        )
                    )
                )

                NetworkModule.api.addMeal(
                    userId = currentUserId,
                    date = dateStr,
                    addMealDto = AddMealDto(meal = mealDto)
                )
                Log.e("UserTraining", "Dodano trening do dziennika: ${caloriesBurned.toInt()} kcal")
                Toast.makeText(requireContext(), "Dodano trening do dziennika: ${caloriesBurned.toInt()} kcal", Toast.LENGTH_LONG).show()
            }
            catch (e: Exception){
                Log.e("UserTraining", "Błąd dodawania treningu", e)
                Toast.makeText(requireContext(), "Błąd: ${e.message}", Toast.LENGTH_SHORT).show()

                isUserInitiatedChange = false
                cbPlanCompleted.isChecked = false
                isUserInitiatedChange = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadExercisesForCurrentPlan()
    }
}