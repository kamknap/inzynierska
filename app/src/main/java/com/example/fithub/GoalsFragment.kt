package com.example.fithub

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment

class GoalsFragment : Fragment(R.layout.fragment_goals) {

    // Proste typy/enumy dla większej czytelności
    enum class MainGoal { LOSE, MAINTAIN, GAIN }
    enum class AppMode { CALORIES, TRAINING, BOTH }
    enum class GoalType { TARGET_WEIGHT, WEEKLY_DELTA }

    data class Goals(
        val mainGoal: MainGoal,
        val appMode: AppMode,
        val goalType: GoalType,
        val targetWeightKg: Double?,   // wypełnione jeśli goalType == TARGET_WEIGHT
        val weeklyDeltaKg: Double?,    // wypełnione jeśli goalType == WEEKLY_DELTA
        val activityLevel1to5: Int,    // 1..5
        // Preferencje treningowe
        val prefStrength: Boolean,
        val prefCardio: Boolean,
        val prefMixed: Boolean,
        // Częstotliwość
        val trainingDaysPerWeek: Int,  // 0..7
        // Powiadomienia
        val notifyMeals: Boolean,
        val notifyTraining: Boolean,
        val notifyWeighIn: Boolean
    )

    // Widoki
    private lateinit var sbMainGoal: SeekBar
    private lateinit var rgAppMode: RadioGroup
    private lateinit var rbCaloriesOnly: RadioButton
    private lateinit var rbTrainingOnly: RadioButton
    private lateinit var rbBoth: RadioButton

    private lateinit var spGoalType: Spinner
    private lateinit var etTargetWeight: EditText
    private lateinit var etWeeklyDelta: EditText

    private lateinit var sbActivityLevel: SeekBar

    private lateinit var cbVegetarian: CheckBox
    private lateinit var cbVegan: CheckBox
    private lateinit var cbGlutenFree: CheckBox
    private lateinit var cbNoDietLimits: CheckBox

    private lateinit var cbStrength: CheckBox
    private lateinit var cbCardio: CheckBox
    private lateinit var cbMixed: CheckBox

    private lateinit var spTrainingFrequency: Spinner

    private lateinit var cbNotifyMeals: CheckBox
    private lateinit var cbNotifyTraining: CheckBox
    private lateinit var cbNotifyWeighIn: CheckBox

    private lateinit var btnConfirm: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- bind ---
        sbMainGoal = view.findViewById(R.id.sbMainGoal)
        rgAppMode = view.findViewById(R.id.rgAppMode)
        rbCaloriesOnly = view.findViewById(R.id.rbCaloriesOnly)
        rbTrainingOnly = view.findViewById(R.id.rbTrainingOnly)
        rbBoth = view.findViewById(R.id.rbBoth)

        spGoalType = view.findViewById(R.id.spGoalType)
        etTargetWeight = view.findViewById(R.id.etTargetWeight)
        etWeeklyDelta = view.findViewById(R.id.etWeeklyDelta)

        sbActivityLevel = view.findViewById(R.id.sbActivityLevel)


        cbStrength = view.findViewById(R.id.cbStrength)
        cbCardio = view.findViewById(R.id.cbCardio)
        cbMixed = view.findViewById(R.id.cbMixed)

        spTrainingFrequency = view.findViewById(R.id.spTrainingFrequency)

        cbNotifyMeals = view.findViewById(R.id.cbNotifyMeals)
        cbNotifyTraining = view.findViewById(R.id.cbNotifyTraining)
        cbNotifyWeighIn = view.findViewById(R.id.cbNotifyWeighIn)

        btnConfirm = view.findViewById(R.id.btnConfirmGoals)

        setupSpinners()
        setupSeekBars()
        setupDietMutualExclusion()
        setupGoalTypeVisibility()
        setupImeActions()
        setupConfirm()
    }

    // Adaptery Spinnerów (możesz podmienić na resources/arrays.xml; poniżej jest wersja „w kodzie”)
    private fun setupSpinners() {
        // Typ celu
        val goalTypes = listOf("Docelowa waga (kg)", "Zmiana tygodniowa (kg/tydz.)")
        spGoalType.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            goalTypes
        )

        // Częstotliwość 0..7 dni/tydz.
        val freq = (0..7).map { "$it dni/tydz." }
        spTrainingFrequency.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            freq
        )
    }

    private fun setupSeekBars() {
        // Główny cel: 0..2; zatrzask po puszczeniu palca
        sbMainGoal.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val p = sbMainGoal.progress.coerceIn(0, 2)
                // ewentualnie „zaokrąglenie” do najbliższej pozycji; przy 3 pozycjach wystarczy coerceIn
                sbMainGoal.progress = p
            }
        })

        // Poziom aktywności: 0..4 -> wyświetlana 1..5; zatrzask niepotrzebny, ale wyrównamy zakres
        sbActivityLevel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val p = sbActivityLevel.progress.coerceIn(0, 4)
                sbActivityLevel.progress = p
            }
        })
    }

    private fun setupDietMutualExclusion() {
        // „Brak ograniczeń” wyklucza inne; zaznaczenie któregokolwiek innego odznacza „Brak ograniczeń”
        cbNoDietLimits.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                cbVegetarian.isChecked = false
                cbVegan.isChecked = false
                cbGlutenFree.isChecked = false
            }
        }
        val uncheckNoLimits: (CompoundButton, Boolean) -> Unit = { _, isChecked ->
            if (isChecked && cbNoDietLimits.isChecked) cbNoDietLimits.isChecked = false
        }
        cbVegetarian.setOnCheckedChangeListener(uncheckNoLimits)
        cbVegan.setOnCheckedChangeListener(uncheckNoLimits)
        cbGlutenFree.setOnCheckedChangeListener(uncheckNoLimits)
    }

    private fun setupGoalTypeVisibility() {
        spGoalType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val type = goalTypeFromPosition(position)
                when (type) {
                    GoalType.TARGET_WEIGHT -> {
                        etTargetWeight.isVisible = true
                        etWeeklyDelta.isGone = true
                        etWeeklyDelta.text?.clear()
                    }
                    GoalType.WEEKLY_DELTA -> {
                        etTargetWeight.isGone = true
                        etWeeklyDelta.isVisible = true
                        etTargetWeight.text?.clear()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupImeActions() {
        // Zatwierdzanie klawiaturą „Done” na polach liczbowych
        etTargetWeight.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                etTargetWeight.clearFocus(); true
            } else false
        }
        etWeeklyDelta.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                etWeeklyDelta.clearFocus(); true
            } else false
        }
    }

    private fun setupConfirm() {
        btnConfirm.setOnClickListener {
            val goals = tryBuildGoals()
            if (goals == null) {
                Toast.makeText(requireContext(), "Uzupełnij wymagane pola", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: Zapisz do swojej bazy (Room / Firebase / serwer).
            // Example placeholder:
            // goalsRepository.save(goals)

            Toast.makeText(requireContext(), "Zapisano cele 🎯", Toast.LENGTH_SHORT).show()

            // Przejście do ekranu głównego (podmień na swój action/id)
            try {
                //przejscie dalej + przeslanie danych do bazy
            } catch (_: Exception) {
                // jeśli jeszcze nie masz nawigacji skonfigurowanej – po prostu zamknij fragment/aktywość
                requireActivity().finish()
            }
        }
    }

    private fun tryBuildGoals(): Goals? {
        // 1) Main goal
        val mainGoal = when (sbMainGoal.progress.coerceIn(0, 2)) {
            0 -> MainGoal.LOSE
            1 -> MainGoal.MAINTAIN
            2 -> MainGoal.GAIN
            else -> MainGoal.MAINTAIN
        }

        // 2) App mode
        val appMode = when {
            rbCaloriesOnly.isChecked -> AppMode.CALORIES
            rbTrainingOnly.isChecked -> AppMode.TRAINING
            rbBoth.isChecked -> AppMode.BOTH
            else -> null
        } ?: return null

        // 3) Goal type & values
        val goalType = goalTypeFromPosition(spGoalType.selectedItemPosition)
        val targetWeight = etTargetWeight.text.toString().toDoubleOrNull()
        val weeklyDelta = etWeeklyDelta.text.toString().toDoubleOrNull()

        when (goalType) {
            GoalType.TARGET_WEIGHT -> if (targetWeight == null || targetWeight <= 0.0) return null
            GoalType.WEEKLY_DELTA -> if (weeklyDelta == null || weeklyDelta == 0.0) return null
        }

        // 4) Activity (1..5)
        val activity = sbActivityLevel.progress.coerceIn(0, 4) + 1


        val prefStrength = cbStrength.isChecked
        val prefCardio = cbCardio.isChecked
        val prefMixed = cbMixed.isChecked

        // 6) Frequency (0..7) – bierzemy indeks spinnera
        val freqIndex = spTrainingFrequency.selectedItemPosition
        val trainingDays = freqIndex.coerceIn(0, 7)

        // 7) Notifications
        val notifyMeals = cbNotifyMeals.isChecked
        val notifyTraining = cbNotifyTraining.isChecked
        val notifyWeighIn = cbNotifyWeighIn.isChecked

        return Goals(
            mainGoal = mainGoal,
            appMode = appMode,
            goalType = goalType,
            targetWeightKg = if (goalType == GoalType.TARGET_WEIGHT) targetWeight else null,
            weeklyDeltaKg = if (goalType == GoalType.WEEKLY_DELTA) weeklyDelta else null,
            activityLevel1to5 = activity,
            prefStrength = prefStrength,
            prefCardio = prefCardio,
            prefMixed = prefMixed,
            trainingDaysPerWeek = trainingDays,
            notifyMeals = notifyMeals,
            notifyTraining = notifyTraining,
            notifyWeighIn = notifyWeighIn
        )
    }

    private fun goalTypeFromPosition(position: Int): GoalType =
        when (position) {
            0 -> GoalType.TARGET_WEIGHT
            1 -> GoalType.WEEKLY_DELTA
            else -> GoalType.TARGET_WEIGHT
        }
}
