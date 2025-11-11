package com.example.fithub

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit

class UserTrainingFragment : Fragment(R.layout.fragment_user_training) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnShowMuscleModel = view.findViewById<Button>(R.id.btnShowMuscleModel)

        btnShowMuscleModel.setOnClickListener {
            openMuscleModel()
        }

        if (savedInstanceState == null) {
            openMuscleModel()
        }
    }

    private fun openMuscleModel() {
        childFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.muscle_model_container, MuscleModelFragment.newInstance())
            addToBackStack(null)
        }
    }

    fun onMuscleClicked(muscleId: String) {
        // Obsługa kliknięcia w mięsień
        Toast.makeText(requireContext(), "Wybrano grupę mięśniową: $muscleId", Toast.LENGTH_SHORT).show()

        // TODO: Tutaj możesz otworzyć listę ćwiczeń dla tej grupy mięśniowej
        // Np. filtrować ćwiczenia po muscleIds
    }
}