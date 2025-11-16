package com.example.fithub

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.fithub.data.GoalPlanData
import com.example.fithub.data.UpdateUserGoalDto
import kotlinx.coroutines.launch

class EditGoalsDialogFragment : DialogFragment() {

    private lateinit var etTargetWeight: EditText
    private lateinit var spGoalType: Spinner
    private lateinit var spTrainingFrequency: Spinner
    private lateinit var alertDialog: AlertDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.fragment_edit_goals_dialog, null)

        etTargetWeight = view.findViewById(R.id.etTargetWeight)
        spGoalType = view.findViewById(R.id.spGoalType)
        spTrainingFrequency = view.findViewById(R.id.spTrainingFrequency)

        setupSpinners()

        // Wypełnij danymi
        arguments?.let {
            etTargetWeight.setText(it.getInt("targetWeight").toString())

            val goalType = it.getString("goalType")
            val goalIndex = when(goalType) {
                "lose_weight" -> 0
                "maintain" -> 1
                "gain_weight" -> 2
                else -> 1
            }
            spGoalType.setSelection(goalIndex)

            spTrainingFrequency.setSelection(it.getInt("trainingFrequency", 3))
        }

        etTargetWeight.setOnClickListener { showWeightPicker() }

        alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Edytuj cele")
            .setView(view)
            .setPositiveButton("Zapisz", null) // Ustawione na null - obsługa niżej
            .setNegativeButton("Anuluj", null)
            .create()

        // Nadpisz domyślne zachowanie przycisku "Zapisz"
        alertDialog.setOnShowListener {
            val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                // NIE zamykaj dialogu automatycznie
                saveGoals()
            }
        }

        return alertDialog
    }

    private fun setupSpinners() {
        // Typ celu
        val goalTypes = arrayOf("Schudnąć", "Utrzymać", "Przytyć")
        spGoalType.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, goalTypes).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Częstotliwość treningów
        val frequencies = (0..7).map { "$it dni/tydz." }
        spTrainingFrequency.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, frequencies).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun saveGoals() {
        val goalId = arguments?.getString("goalId")
        val userId = arguments?.getString("userId") ?: return

        if (goalId == null) {
            Toast.makeText(context, "Brak aktywnego celu do edycji", Toast.LENGTH_SHORT).show()
            dismiss()
            return
        }

        // Wyłącz przyciski podczas zapisywania
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled = false

        lifecycleScope.launch {
            try {
                val targetWeight = etTargetWeight.text.toString().toIntOrNull()

                if (targetWeight == null) {
                    Toast.makeText(context, "Nieprawidłowa waga docelowa", Toast.LENGTH_SHORT).show()
                    // Włącz przyciski z powrotem
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled = true
                    return@launch
                }

                val goalTypeApi = when(spGoalType.selectedItemPosition) {
                    0 -> "lose_weight"
                    1 -> "maintain"
                    2 -> "gain_weight"
                    else -> "maintain"
                }

                val updateDto = UpdateUserGoalDto(
                    type = goalTypeApi,
                    targetWeightKg = targetWeight,
                    plan = GoalPlanData(
                        trainingFrequencyPerWeek = spTrainingFrequency.selectedItemPosition,
                        estimatedDurationWeeks = null,
                        calorieTarget = null
                    )
                )

                Log.d("EditGoals", "Wysyłam dane: $updateDto")

                val response = NetworkModule.api.updateUserGoal(goalId, updateDto)

                Log.d("EditGoals", "Odpowiedź: $response")

                Toast.makeText(context, "Cele zaktualizowane", Toast.LENGTH_SHORT).show()

                // Odśwież fragment rodzica
                (parentFragment as? UserProfileFragment)?.loadDataForUser(userId)

                // Zamknij dialog DOPIERO TERAZ
                dismiss()

            } catch (e: Exception) {
                Log.e("EditGoals", "Błąd aktualizacji: ${e.message}", e)
                Toast.makeText(context, "Błąd: ${e.message}", Toast.LENGTH_LONG).show()

                // Włącz przyciski z powrotem w przypadku błędu
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled = true
            }
        }
    }

    private fun showWeightPicker() {
        val picker = NumberPicker(requireContext()).apply {
            minValue = 30
            maxValue = 200
            value = etTargetWeight.text.toString().toIntOrNull() ?: 70
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Waga docelowa")
            .setView(picker)
            .setPositiveButton("OK") { _, _ ->
                etTargetWeight.setText(picker.value.toString())
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }
}