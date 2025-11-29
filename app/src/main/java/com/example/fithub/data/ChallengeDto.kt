package com.example.fithub.data

import com.google.gson.annotations.SerializedName

data class ChallengeDto(
    @SerializedName("_id") val id: String? = null,
    val name: String,
    val desc: String,
    val pointsForComplete: Int,
    val type: ChallengeType,
    val targetValue: Int
)

enum class ChallengeType {
    STREAK,      // Logowanie dzień po dniu
    MEAL_COUNT,  // Liczba dodanych posiłków
    WEIGHT_LOSS,  // Ilość zrzuconych kilogramów (różnica start vs current)
    TRAINING_COUNT // Liczba wykonanych treningow
}
