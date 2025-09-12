package com.example.fithub

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.fithub.data.*
import com.example.fithub.logic.UserCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant

class GoalsFragment : Fragment(R.layout.fragment_user_goals) {

    // Views
    private lateinit var sbMainGoal: SeekBar
    private lateinit var etTargetWeight: EditText
    private lateinit var sbActivityLevel: SeekBar
    private lateinit var spTrainingFrequency: Spinner
    private lateinit var cbNotifyMeals: CheckBox
    private lateinit var cbNotifyTraining: CheckBox
    private lateinit var cbNotifyWeighIn: CheckBox
    private lateinit var btnConfirmGoals: Button
    private var userData: UserData? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            userData = UserData(
                name = bundle.getString("userName", ""),
                sex = bundle.getString("userSex", ""),
                age = bundle.getInt("userAge", 0),
                weight = bundle.getDouble("userWeight", 0.0),
                height = bundle.getDouble("userHeight", 0.0)
            )
        }

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

        // Mapowanie z index na enum
        val mainGoal = when(mainGoalIndex) {
            0 -> MainGoal.LOSE
            1 -> MainGoal.MAINTAIN
            2 -> MainGoal.GAIN
            else -> MainGoal.MAINTAIN
        }

        return GoalsData(
            mainGoal = mainGoal,
            targetWeightKg = targetWeight,
            activityLevel = activityLevel,
            trainingFrequencyPerWeek = freq,
            notifyMeals = cbNotifyMeals.isChecked,
            notifyTraining = cbNotifyTraining.isChecked,
            notifyWeighIn = cbNotifyWeighIn.isChecked
        )
    }

    //zapisywanie do mongo
    private fun saveGoals(goals: GoalsData) {
        lifecycleScope.launch {
            try {
                userData?.let { user ->
                    val userCalculator = UserCalculator()
                    val bmi = userCalculator.calculateBMI(user.weight!!, user.height!!)
                    val roundedBMI = String.format("%.1f", bmi).toDouble()
                    val bmr = userCalculator.calculateBMR(
                        user.weight!!, user.height!!, user.age!!.toDouble(), user.sex
                    )
                    val roundedBMR = String.format("%.0f", bmr).toDouble()

                    // Przygotuj dane użytkownika
                    val createUserDto = CreateUserDto(
                        username = user.name,
                        auth = AuthData(
                            provider = "local", // tymczasowo, póki nie ma Firebase
                            firebaseUid = null
                        ),
                        profile = ProfileData(
                            sex = user.sex,
                            age = user.age!!,
                            heightCm = user.height!!.toInt(),
                            weightKg = user.weight!!.toInt()
                        ),
                        computed = ComputedData(
                            bmi = roundedBMI,
                            bmr = roundedBMR
                        ),
                        settings = SettingsData(
                            activityLevel = goals.activityLevel,
                            notifications = NotificationSettings(
                                enabled = goals.notifyMeals || goals.notifyTraining || goals.notifyWeighIn,
                                types = NotificationTypes(
                                    workoutReminders = goals.notifyTraining,
                                    mealReminders = goals.notifyMeals,
                                    measureReminders = goals.notifyWeighIn
                                ),
                                channels = NotificationChannels(
                                    push = true,
                                    email = false
                                )
                            ),
                            preferredTrainingFrequencyPerWeek = goals.trainingFrequencyPerWeek
                        )
                    )

                    // Zapisz użytkownika
                    val createdUser = NetworkModule.api.createUser(createUserDto)

                    // Oblicz szacowany czas trwania (w tygodniach)
                    val currentWeight = user.weight!!
                    val targetWeight = goals.targetWeight ?: currentWeight
                    val weightDifference = kotlin.math.abs(currentWeight - targetWeight)
                    
                    // Bezpieczna utrata/przyrost: 0.5kg/tydzień
                    val estimatedWeeks = if (weightDifference > 0) {
                        (weightDifference / 0.5).toInt().coerceIn(1, 52) // minimum 1 tydzień, maksimum rok
                    } else {
                        12 // domyślnie 12 tygodni dla utrzymania wagi
                    }

                    // Oblicz cel kaloryczny na podstawie BMR i poziomu aktywności
                    val activityMultiplier = when(goals.activityLevel) {
                        1 -> 1.2   // Brak aktywności/siedzący tryb życia
                        2 -> 1.375 // Lekka aktywność (1-3 dni/tydzień)
                        3 -> 1.55  // Umiarkowana aktywność (3-5 dni/tydzień)
                        4 -> 1.725 // Wysoka aktywność (6-7 dni/tydzień)
                        5 -> 1.9   // Bardzo wysoka aktywność (2x dziennie, intensywne treningi)
                        else -> 1.55
                    }
                    
                    val tdee = (roundedBMR * activityMultiplier).toInt()
                    val calorieTarget = when(goals.mainGoalKey) {
                        "lose_weight" -> tdee - 400  // deficyt 400 kcal
                        "gain_weight" -> tdee + 300  // nadwyżka 300 kcal
                        else -> tdee                 // utrzymanie
                    }

                    // Przygotuj dane celu
                    val createGoalDto = CreateUserGoalDto(
                        userId = createdUser.id,
                        type = when(goals.mainGoalKey) {
                            "lose_weight" -> "lose_weight"
                            "gain_weight" -> "gain_weight"
                            "maintain" -> "maintain"
                            else -> "maintain"
                        },
                        targetWeightKg = goals.targetWeight?.toInt() ?: 0,
                        plan = GoalPlanData(
                            trainingFrequencyPerWeek = goals.trainingFrequencyPerWeek,
                            estimatedDurationWeeks = estimatedWeeks,
                            calorieTarget = calorieTarget
                        ),
                        startedAt = java.time.Instant.now().toString(),
                        notes = "Utworzono podczas onboardingu"
                    )

                    // Zapisz cel
                    NetworkModule.api.createUserGoal(createGoalDto)

                    Toast.makeText(requireContext(), "Dane i cele zapisane", Toast.LENGTH_SHORT).show()
                    requireActivity().finish()
                } ?: run {
                    Toast.makeText(requireContext(), "Brak danych użytkownika", Toast.LENGTH_SHORT).show()
                }
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
