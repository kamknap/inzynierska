package com.example.fithub

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.fithub.data.UserGoalDto
import kotlinx.coroutines.launch


class UserWeightFragment : Fragment(R.layout.fragment_user_weight) {

    private lateinit var userWeight: TextView
    private lateinit var userProgress: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userWeight = view.findViewById(R.id.tvCurrentWeight)
        userProgress = view.findViewById(R.id.tvProgress)
        val userId = "68cbc06e6cdfa7faa8561f82"
        getCurrentWeight(userId)
        updateProgressStats(userId)
    }

    private fun getCurrentWeight(userId: String){
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = NetworkModule.api.getUserById(userId)
                userWeight.text = "Obecna waga: ${user.profile.weightKg}kg"
            }
            catch (e: Exception){
                Toast.makeText(
                    requireContext(),
                    "Błąd: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateProgressStats(userId: String){
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = NetworkModule.api.getUserById(userId)
                val userGoals = NetworkModule.api.getUserGoalsByUserId(userId)

                if (userGoals.isEmpty()) {
                    userProgress.text = "Brak aktywnego celu"
                    return@launch
                }

                val currentWeight = user.profile.weightKg

                val activeGoal = userGoals.firstOrNull { it.status == "active" }
                    ?: userGoals.first()

                val startWeight = activeGoal.firstWeightKg
                val targetWeight = activeGoal.targetWeightKg
                val goalType = activeGoal.type

                when (goalType){
                    "lose_weight" -> {
                        val remaining = currentWeight - targetWeight
                        val progress = startWeight - currentWeight
                        userProgress.text = "Schudnięto: ${progress}kg, pozostało: ${remaining}kg"
                    }
                    "gain_weight" -> {
                        val remaining = targetWeight - currentWeight
                        val progress = currentWeight - startWeight
                        userProgress.text = "Przybrano: ${progress}kg, pozostało: ${remaining}kg"
                    }
                    "maintain" -> {
                        val difference = startWeight - currentWeight
                        userProgress.text = "Różnica względem wagi początkowej: ${difference}kg"
                    }
                }
            }
            catch (e: Exception){
                Log.e("UserWeightFragment", "Błąd: ${e.message}", e)
                Toast.makeText(
                    requireContext(),
                    "Błąd: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}