package com.example.fithub.data

import com.google.gson.annotations.SerializedName

data class UserGoalDto(
    @SerializedName("_id") val id: String,
    val userId: UserIdData, // Zmienione z String na obiekt
    val type: String,
    val targetWeightKg: Int,
    val plan: GoalPlanData,
    val status: String,
    val startedAt: String,
    val completedAt: String?,
    val notes: String?,
    val createdAt: String,
    val updatedAt: String
)

// Nowy DTO dla zagnieżdżonego userId
data class UserIdData(
    @SerializedName("_id") val id: String,
    val username: String,
    val profile: ProfileData
)

data class GoalPlanData(
    val trainingFrequencyPerWeek: Int,
    val estimatedDurationWeeks: Int?,
    val calorieTarget: Int?
)

data class CreateUserGoalDto(
    val userId: String, // Pozostaje jako String bo to do wysyłania
    val type: String,
    val targetWeightKg: Int,
    val plan: GoalPlanData,
    val status: String = "active",
    val startedAt: String,
    val notes: String? = null
)
