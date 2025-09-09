package com.example.fithub.data

data class UserDto(
    val _id: String,
    val username: String,
    val sex: String,
    val age: Int,
    val weight: Int,
    val height: Int,
    val bmr: Double,
    val bmi: Double
)
