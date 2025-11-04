package com.example.fithub

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.fithub.data.UpdateProfileData
import com.example.fithub.data.UpdateUserDto
import com.example.fithub.data.UserGoalDto
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// TODO - ustandaryzować jednostkę wagi INT albo DOUBLE i wszedzie zmienić, wykres zsynchronizowany z przyciskami
class UserWeightFragment : Fragment(R.layout.fragment_user_weight) {

    private lateinit var userWeight: TextView
    private lateinit var userProgress: TextView
    private lateinit var btnAddWeight: ImageButton
    private lateinit var weightHistoryAdapter: WeightHistoryAdapter
    private lateinit var rvWeightHistory : RecyclerView
    private var currentUserWeight: Double? = null
    private var currentGoalType: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userWeight = view.findViewById(R.id.tvCurrentWeight)
        userProgress = view.findViewById(R.id.tvProgress)
        btnAddWeight = view.findViewById(R.id.btnAddWeight)
        rvWeightHistory = view.findViewById(R.id.rvWeightHistory)

        val userId = "68cbc06e6cdfa7faa8561f82"
        getCurrentWeight(userId)
        updateProgressStats(userId)
        fillHistory(userId)

        btnAddWeight.setOnClickListener {
            showAddWeightDialog(userId)
        }
    }

    private fun getCurrentWeight(userId: String){
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = NetworkModule.api.getUserById(userId)
                val userGoals = NetworkModule.api.getUserGoalsByUserId(userId)

                if (userGoals.isEmpty()) {
                    return@launch
                }

                currentUserWeight = user.profile.weightKg.toDouble()

                val activeGoal = userGoals.firstOrNull { it.status == "active" }
                    ?: userGoals.first()

                currentGoalType = activeGoal.type

                userWeight.text = "Obecna waga: ${user.profile.weightKg}kg, cel: ${activeGoal.targetWeightKg}kg"
                updateAdapter()
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

                currentGoalType = goalType

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

    private fun showAddWeightDialog(userId: String) {
        val dialogLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }

        val inputLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val etWeight = EditText(requireContext()).apply {
            hint = "Waga"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText("")
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val unitTextView = TextView(requireContext()).apply {
            text = " kg"
            textSize = 18F
            setPadding(8, 0, 0, 0)
        }

        inputLayout.addView(etWeight)
        inputLayout.addView(unitTextView)

        val dateLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(0, 16, 0, 0)
        }

        val tvDateLabel = TextView(requireContext()).apply {
            text = "Data pomiaru: "
            textSize = 16F
        }

        val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        val etDate = TextView(requireContext()).apply {
            text = currentDate
            textSize = 16F
            setPadding(8, 0, 0, 0)
            setTextColor(resources.getColor(android.R.color.holo_blue_dark, null))
        }

        dateLayout.addView(tvDateLabel)
        dateLayout.addView(etDate)

        dialogLayout.addView(inputLayout)
        dialogLayout.addView(dateLayout)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Dodaj pomiar wagi")
            .setView(dialogLayout)
            .setPositiveButton("Dodaj") { _, _ ->
                val weight = etWeight.text.toString().toDoubleOrNull()

                if (weight == null || weight <= 0) {
                    Toast.makeText(requireContext(), "Podaj prawidłową wagę", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                addWeight(userId, weight)
            }
            .setNegativeButton("Anuluj", null)
            .create()

        dialog.setOnShowListener {
            etWeight.requestFocus()
            etWeight.postDelayed({
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                        as android.view.inputmethod.InputMethodManager
                imm.showSoftInput(etWeight, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            }, 100)
        }

        dialog.show()
    }

    private fun addWeight(userId: String, weight: Double){
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val currentUser = NetworkModule.api.getUserById(userId)

                val updateUserDto = UpdateUserDto(
                    profile = UpdateProfileData(
                        sex = currentUser.profile.sex,
                        birthDate = currentUser.profile.birthDate,
                        heightCm = currentUser.profile.heightCm,
                        weightKg = weight.toInt()
                    )
                )
                NetworkModule.api.updateUser(userId, updateUserDto)

                Toast.makeText(
                    requireContext(),
                    "Waga zaktualizowana: ${String.format("%.1f", weight)}kg",
                    Toast.LENGTH_SHORT
                ).show()

                getCurrentWeight(userId)
                updateProgressStats(userId)
                fillHistory(userId)
            }
            catch (e: Exception){
                Log.e("UserWeightFragment", "Błąd dodawania pomiaru", e)
                Toast.makeText(
                    requireContext(),
                    "Błąd: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun fillHistory(userId: String){
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userHistoryList = NetworkModule.api.getUserWeightHistory(userId)

                val sortedList = userHistoryList.sortedByDescending { it.measuredAt }

                weightHistoryAdapter = WeightHistoryAdapter(currentUserWeight, currentGoalType)
                rvWeightHistory.apply {
                    layoutManager = LinearLayoutManager(requireContext())
                    adapter = weightHistoryAdapter
                }
                weightHistoryAdapter.submitList(sortedList)

                Log.d("WeightHistory", "Załadowano ${sortedList.size} pomiarów")
            }
            catch(e: Exception){
                Log.e("UserWeightFragment", "Bład", e)
            }
        }
    }

    private fun updateAdapter() {
        if (::weightHistoryAdapter.isInitialized) {
            weightHistoryAdapter = WeightHistoryAdapter(currentUserWeight, currentGoalType)
            rvWeightHistory.adapter = weightHistoryAdapter
            fillHistory("68cbc06e6cdfa7faa8561f82")
        }
    }
}