package com.example.fithub.data
import androidx.annotation.IntRange


data class GoalsData(
    val mainGoal: MainGoal = MainGoal.MAINTAIN,     // schudnąć/utrzymać/przytyć
    val targetWeightKg: Double? = null,             // docelowa waga
    @IntRange(from = 1, to = 5)
    val activityLevel: Int = 1,                     // 1..5 (zgodnie z UI)
    @IntRange(from = 0, to = 7)
    val trainingFrequencyPerWeek: Int = 3,          // 0..7
    val notifyMeals: Boolean = false,
    val notifyTraining: Boolean = false,
    val notifyWeighIn: Boolean = false
){
    fun isComplete(): Boolean = targetWeightKg != null

    fun isValidRanges(): Boolean {
        val wOk = (targetWeightKg ?: -1.0) in 30.0..300.0
        val aOk = activityLevel in 1..5
        val fOk = trainingFrequencyPerWeek in 0..7
        return wOk && aOk && fOk
    }
}

enum class MainGoal(val apiKey: String) {
    LOSE("lose"),
    MAINTAIN("maintain"),
    GAIN("gain")
}
