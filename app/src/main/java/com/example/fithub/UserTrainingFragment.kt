package com.example.fithub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
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

    val currentUserId = "68cbc06e6cdfa7faa8561f82"
    private val currentPlanName = "Plan treningowy 1"
    private var currentPlanId = "6914d3aa39f1130be1acad07"

    private val exercisesInPlan = mutableListOf<ExerciseListAdapter.ExerciseItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnShowMuscleModel = view.findViewById(R.id.btnShowMuscleModel)
        btnSearchExercise = view.findViewById(R.id.btnSearchExercise)
        rvUserExercises = view.findViewById(R.id.rvUserExercises)


        btnShowMuscleModel.setOnClickListener {
            openMuscleModel()
        }

        btnSearchExercise.setOnClickListener {
            openAddExerciseToPlanDialog()
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
}