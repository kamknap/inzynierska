package com.example.fithub.data
import androidx.annotation.IntRange

data class GoalsData(
    val mainGoal: MainGoal = MainGoal.MAINTAIN,
    val targetWeightKg: Double? = null,
    @IntRange(from = 1, to = 5)
    val activityLevel: Int = 1,
    @IntRange(from = 0, to = 7)
    val trainingFrequencyPerWeek: Int = 3,
    val notifyMeals: Boolean = false,
    val notifyTraining: Boolean = false,
    val notifyWeighIn: Boolean = false
) {
    val mainGoalKey: String get() = mainGoal.apiKey
    val mainGoalIndex: Int get() = when(mainGoal) {
        MainGoal.LOSE -> 0
        MainGoal.MAINTAIN -> 1
        MainGoal.GAIN -> 2
    }
    val mainGoalLabel: String get() = when(mainGoal) {
        MainGoal.LOSE -> "Schudnąć"
        MainGoal.MAINTAIN -> "Utrzymać"
        MainGoal.GAIN -> "Przytyć"
    }
    val targetWeight: Double? get() = targetWeightKg
    
    fun isComplete(): Boolean = targetWeightKg != null

    fun isValidRanges(): Boolean {
        val wOk = (targetWeightKg ?: -1.0) in 30.0..300.0
        val aOk = activityLevel in 1..5
        val fOk = trainingFrequencyPerWeek in 0..7
        return wOk && aOk && fOk
    }

    fun toDto(): AddGoalsDto = AddGoalsDto(
        mainGoal = mainGoalKey,
        targetWeight = targetWeightKg?.toInt() ?: 0,
        activityLevel = activityLevel,
        trainingFrequencyPerWeek = trainingFrequencyPerWeek,
        notifyMeals = notifyMeals,
        notifyTraining = notifyTraining,
        notifyWeighIn = notifyWeighIn
    )
}

data class AddGoalsDto(
    val mainGoal: String,              // "lose_weight" | "maintain" | "gain_weight"
    val targetWeight: Int,             // kg
    val activityLevel: Int,            // 1..5
    val trainingFrequencyPerWeek: Int, // 0..7
    val notifyMeals: Boolean,
    val notifyTraining: Boolean,
    val notifyWeighIn: Boolean
)

enum class MainGoal(val apiKey: String) {
    LOSE("lose_weight"),
    MAINTAIN("maintain"),
    GAIN("gain_weight")
}
