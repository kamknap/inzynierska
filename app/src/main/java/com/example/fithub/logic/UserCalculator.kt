package com.example.fithub.logic

class UserCalculator {
     fun calculateBMI(weight: Double, height: Double): Double? {
        if (weight <= 0 || height <= 0) return null
        val heightInMeters = height / 100
        return weight / (heightInMeters * heightInMeters)
    }

    fun getBMICategory(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Niedowaga"
            bmi < 25.0 -> "Prawidłowa waga"
            bmi < 30.0 -> "Nadwaga"
            else -> "Otyłość"
        }
    }

    //todo dodać bmr
}