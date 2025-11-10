package com.example.fithub.data

data class ExerciseFoodDto(
    val exerciseId: String,
    val name: String,
    val duration: Int,
    val caloriesBurned: Double,
    val weight: Double,
    val mets: Double
)
