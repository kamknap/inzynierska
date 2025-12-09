package com.example.fithub.data

data class UpdateUserGoalDto(
    val type: String? = null,
    val firstWeightKg: Double? = null,
    val targetWeightKg: Double? = null,
    val plan: GoalPlanData? = null,
    val status: String? = null,
    val completedAt: String? = null,
    val notes: String? = null
)