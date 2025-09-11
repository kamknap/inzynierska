package com.example.fithub

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GoalsFragment : Fragment(R.layout.fragment_goals) {

    // Views
    private lateinit var sbMainGoal: SeekBar
    private lateinit var etTargetWeight: EditText
    private lateinit var sbActivityLevel: SeekBar
    private lateinit var spTrainingFrequency: Spinner
    private lateinit var cbNotifyMeals: CheckBox
    private lateinit var cbNotifyTraining: CheckBox
    private lateinit var cbNotifyWeighIn: CheckBox
    private lateinit var btnConfirmGoals: Button

    // Stałe mapowania
    private val mainGoalLabels = arrayOf("Schudnąć", "Utrzymać", "Przytyć")
    private val mainGoalKeys   = arrayOf("lose", "maintain", "gain") // na potrzeby DTO

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupFrequencySpinner()
        setupClickListeners()
        setupTextWatchers()
    }

    private fun initViews(view: View) {
        sbMainGoal = view.findViewById(R.id.sbMainGoal)
        etTargetWeight = view.findViewById(R.id.etTargetWeight)
        sbActivityLevel = view.findViewById(R.id.sbActivityLevel)
        spTrainingFrequency = view.findViewById(R.id.spTrainingFrequency)
        cbNotifyMeals = view.findViewById(R.id.cbNotifyMeals)
        cbNotifyTraining = view.findViewById(R.id.cbNotifyTraining)
        cbNotifyWeighIn = view.findViewById(R.id.cbNotifyWeighIn)
        btnConfirmGoals = view.findViewById(R.id.btnConfirmGoals)
    }

    private fun setupFrequencySpinner() {
        // 0..7 dni/tydz.
        val items = (0..7).map { if (it == 0) "0 dni/tydz." else "$it dni/tydz." }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            items
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spTrainingFrequency.adapter = adapter
        spTrainingFrequency.setSelection(3, false) // domyślnie 3 dni/tydz.
    }

    private fun setupClickListeners() {
        // opcjonalnie: picker dla docelowej wagi (integer kg)
        etTargetWeight.setOnClickListener { showTargetWeightPicker() }

        btnConfirmGoals.setOnClickListener {
            val goals = getGoalsData()
            if (!goals.isComplete()) {
                Toast.makeText(requireContext(), "Uzupełnij docelową wagę", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Walidacje pomocnicze (zakresy)
            if (!goals.isValidRanges()) {
                Toast.makeText(requireContext(), "Sprawdź zakresy pól (waga 30–300 kg, aktywność 1–5, trening 0–7).", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            saveGoals(goals)
        }
    }

    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { /* tu można dodać podpowiedzi, np. walidację on-the-fly */ }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        etTargetWeight.addTextChangedListener(watcher)
    }

    private fun getGoalsData(): GoalsData {
        val mainGoalIndex = sbMainGoal.progress.coerceIn(0, 2) // 0..2
        val targetWeight = etTargetWeight.text.toString().trim().toDoubleOrNull()
        val activityLevel = (sbActivityLevel.progress + 1).coerceIn(1, 5) // seekbar 0..4 -> 1..5
        val freq = spTrainingFrequency.selectedItemPosition.coerceIn(0, 7) // bo wstawiamy 0..7 po kolei

        return GoalsData(
            mainGoalIndex = mainGoalIndex,
            mainGoalLabel = mainGoalLabels[mainGoalIndex],
            mainGoalKey = mainGoalKeys[mainGoalIndex],
            targetWeight = targetWeight,
            activityLevel = activityLevel,
            trainingFrequencyPerWeek = freq,
            notifyMeals = cbNotifyMeals.isChecked,
            notifyTraining = cbNotifyTraining.isChecked,
            notifyWeighIn = cbNotifyWeighIn.isChecked
        )
    }

    private fun saveGoals(goals: GoalsData) {
        val dto = goals.toDto()

        lifecycleScope.launch {
            try {
                // TODO: Podmień na swoją implementację zapisu:
                // NetworkModule.api.saveGoals(dto)
                // Tymczasowa symulacja requestu:
                delay(300)

                Toast.makeText(requireContext(), "Ustawienia zapisane", Toast.LENGTH_SHORT).show()
                // Zakończ onboarding lub przejdź dalej:
                requireActivity().finish()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Błąd: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- Pickery ---

    private fun showTargetWeightPicker() {
        showNumberPicker(
            title = "Wybierz docelową wagę (kg)",
            currentValue = etTargetWeight.text.toString().toIntOrNull() ?: 70,
            minValue = 30,
            maxValue = 300
        ) { value ->
            etTargetWeight.setText(value.toString())
        }
    }

    private fun showNumberPicker(
        title: String,
        currentValue: Int,
        minValue: Int,
        maxValue: Int,
        onValueSelected: (Int) -> Unit
    ) {
        val numberPicker = NumberPicker(requireContext()).apply {
            this.minValue = minValue
            this.maxValue = maxValue
            value = currentValue.coerceIn(minValue, maxValue)
            wrapSelectorWheel = false
        }

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(numberPicker)
            .setPositiveButton("OK") { _, _ ->
                onValueSelected(numberPicker.value)
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }
}

/** Model danych ekranu celów + prosta walidacja i mapowanie do DTO */
data class GoalsData(
    val mainGoalIndex: Int,               // 0: Schudnąć, 1: Utrzymać, 2: Przytyć
    val mainGoalLabel: String,            // do UI/logów
    val mainGoalKey: String,              // do API: "lose"/"maintain"/"gain"
    val targetWeight: Double?,            // kg
    val activityLevel: Int,               // 1..5
    val trainingFrequencyPerWeek: Int,    // 0..7
    val notifyMeals: Boolean,
    val notifyTraining: Boolean,
    val notifyWeighIn: Boolean
) {
    fun isComplete(): Boolean = targetWeight != null
    fun isValidRanges(): Boolean {
        val wOk = (targetWeight ?: -1.0) in 30.0..300.0
        val aOk = activityLevel in 1..5
        val fOk = trainingFrequencyPerWeek in 0..7
        return wOk && aOk && fOk
    }

    fun toDto(): AddGoalsDto = AddGoalsDto(
        mainGoal = mainGoalKey,
        targetWeight = targetWeight?.toInt() ?: 0,
        activityLevel = activityLevel,
        trainingFrequencyPerWeek = trainingFrequencyPerWeek,
        notifyMeals = notifyMeals,
        notifyTraining = notifyTraining,
        notifyWeighIn = notifyWeighIn
    )
}

/** Przykładowy DTO pod API — dopasuj nazwy pól do swojego backendu */
data class AddGoalsDto(
    val mainGoal: String,              // "lose" | "maintain" | "gain"
    val targetWeight: Int,             // kg (int jak w UserData)
    val activityLevel: Int,            // 1..5
    val trainingFrequencyPerWeek: Int, // 0..7
    val notifyMeals: Boolean,
    val notifyTraining: Boolean,
    val notifyWeighIn: Boolean
)
