package com.example.fithub.data

import com.google.gson.annotations.SerializedName

data class UserGoalsDto(
    val userId: String,                           // _id użytkownika
    val type: String,                             // "lose_weight" | "gain_weight" | "maintain"
    @SerializedName("targetWeightKg") val targetWeightKg: Int,
    val plan: GoalPlan? = null,
    val status: String = "active",                // domyślnie "active"
    val startedAt: String                         // ISO-8601, np. Instant.now().toString()
)

data class GoalPlan(
    val trainingFrequencyPerWeek: Int,
    val estimatedDurationWeeks: Int? = null,
    val calorieTarget: Int? = null
)

data class UserGoalResponse(
    @SerializedName("_id") val id: String,
    val userId: String,
    val type: String
)
