package pl.fithubapp

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import pl.fithubapp.data.GoalPlanData
import pl.fithubapp.data.UpdateUserGoalDto
import kotlinx.coroutines.launch

class EditGoalsDialogFragment : DialogFragment() {

    private lateinit var etTargetWeight: com.google.android.material.textfield.TextInputEditText
    private lateinit var spGoalType: Spinner
    private lateinit var spTrainingFrequency: Spinner
    private lateinit var alertDialog: AlertDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.fragment_edit_goals_dialog, null)

        etTargetWeight = view.findViewById(R.id.etTargetWeight)
        spGoalType = view.findViewById(R.id.spGoalType)
        spTrainingFrequency = view.findViewById(R.id.spTrainingFrequency)

        setupSpinners()

        arguments?.let {
            etTargetWeight.setText(it.getDouble("targetWeight").toInt().toString())

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

        alertDialog = AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_Fithub_Dialog)
            .setTitle("Edytuj cele")
            .setView(view)
            .setPositiveButton("Zapisz", null)
            .setNegativeButton("Anuluj", null)
            .create()

        alertDialog.setOnShowListener {
            val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                saveGoals()
            }
        }

        return alertDialog
    }

    private fun setupSpinners() {
        // Typ celu
        val goalTypes = arrayOf("Schudnąć", "Utrzymać", "Przytyć")
        spGoalType.adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, goalTypes).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item)
        }

        // Częstotliwość treningów
        val frequencies = (0..7).map { "$it dni/tydz." }
        spTrainingFrequency.adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, frequencies).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item)
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

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled = false

        lifecycleScope.launch {
            try {
                val targetWeight = etTargetWeight.text.toString().toIntOrNull()

                if (targetWeight == null) {
                    Toast.makeText(context, "Nieprawidłowa waga docelowa", Toast.LENGTH_SHORT).show()
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

                val user = NetworkModule.api.getCurrentUser()
                val bmr = user.computed.bmr.toInt()
                
                val activityLevel = user.settings?.activityLevel ?: 3
                
                val activityMultiplier = when(activityLevel) {
                    1 -> 1.2   // Brak aktywności/siedzący tryb życia
                    2 -> 1.375 // Lekka aktywność (1-3 dni/tydzień)
                    3 -> 1.55  // Umiarkowana aktywność (3-5 dni/tydzień)
                    4 -> 1.725 // Wysoka aktywność (6-7 dni/tydzień)
                    5 -> 1.9   // Bardzo wysoka aktywność (2x dziennie, intensywne treningi)
                    else -> 1.55
                }
                
                val tdee = (bmr * activityMultiplier).toInt()
                
                val calorieTarget = when(goalTypeApi) {
                    "lose_weight" -> tdee - 400  // deficyt 400 kcal
                    "gain_weight" -> tdee + 300  // nadwyżka 300 kcal
                    else -> tdee                 // utrzymanie
                }

                val updateDto = UpdateUserGoalDto(
                    type = goalTypeApi,
                    targetWeightKg = targetWeight.toDouble(),
                    plan = GoalPlanData(
                        trainingFrequencyPerWeek = spTrainingFrequency.selectedItemPosition,
                        estimatedDurationWeeks = null,
                        calorieTarget = calorieTarget
                    )
                )

                Log.d("EditGoals", "Wysyłam dane: $updateDto")

                val response = NetworkModule.api.updateUserGoal(goalId, updateDto)

                Log.d("EditGoals", "Odpowiedź: $response")

                Toast.makeText(context, "Cele zaktualizowane", Toast.LENGTH_SHORT).show()

                (parentFragment as? UserProfileFragment)?.loadDataForUser()

                dismiss()

            } catch (e: Exception) {
                Log.e("EditGoals", "Błąd aktualizacji: ${e.message}", e)
                Toast.makeText(context, "Błąd: ${e.message}", Toast.LENGTH_LONG).show()

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
            try {
                val selectorWheelPaintField = NumberPicker::class.java.getDeclaredField("mSelectorWheelPaint")
                selectorWheelPaintField.isAccessible = true
                val paint = selectorWheelPaintField.get(this) as? android.graphics.Paint
                paint?.color = android.graphics.Color.parseColor("#212121")
                paint?.textSize = 60f
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_Fithub_Dialog)
            .setTitle("Waga docelowa")
            .setView(picker)
            .setPositiveButton("OK") { _, _ ->
                etTargetWeight.setText(picker.value.toString())
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }
}