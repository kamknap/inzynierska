package com.example.fithub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.compose.runtime.currentRecomposeScope
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fithub.data.ExerciseDto
import kotlinx.coroutines.launch

class UserTrainingFragment : Fragment(R.layout.fragment_user_training) {

    private lateinit var btnShowMuscleModel: Button
    private lateinit var btnSearchExercise: Button
    private lateinit var rvUserExercises: RecyclerView
    private lateinit var exerciseAdapter: ExerciseListAdapter
    private lateinit var exercisePlanName: TextView


    val currentUserId = "68cbc06e6cdfa7faa8561f82"
    private var currentPlanName = "Plan treningowy 1"
    private var currentPlanId =""

    private val exercisesInPlan = mutableListOf<ExerciseListAdapter.ExerciseItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnShowMuscleModel = view.findViewById(R.id.btnShowMuscleModel)
        btnSearchExercise = view.findViewById(R.id.btnSearchExercise)
        rvUserExercises = view.findViewById(R.id.rvUserExercises)
        exercisePlanName = view.findViewById(R.id.tvExercisePlanName)

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
            lifecycle = viewLifecycleOwner.lifecycle // Dodaj lifecycle
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

    override fun onResume() {
        super.onResume()
        loadExercisesForCurrentPlan()
    }
}