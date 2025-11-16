package com.example.fithub.logic

import java.time.LocalDate
import java.time.Year

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

    fun calculateBMR(weight: Double, height: Double, age: Double, sex: String): Double?{
        if (weight <= 0 || height <= 0 || age <= 0) return null
        return when (sex) {
            "Male" -> (13.397 * weight) + (4.799 * height) - (5.677 * age) + 88.362
            "Female" -> (9.247 * weight) + (3.098 * height) - (4.330 * age) + 447.593
            else -> null
        }
    }

    fun calculateEnergyExpenditure(weight: Double, mets: Double, minutes: Double): Double? {
        if (weight <= 0 || mets <= 0 || minutes <= 0) return null
        return ((mets * 3.5 * weight) / 200) * minutes
    }

    fun calculateAge(birthDate: String): Int {
        return try {
            val date = if (birthDate.contains("T")) {
                LocalDate.parse(birthDate.substring(0, 10))
            } else {
                LocalDate.parse(birthDate)
            }

            val today = LocalDate.now()
            var age = today.year - date.year

            if (today.monthValue < date.monthValue ||
                (today.monthValue == date.monthValue && today.dayOfMonth < date.dayOfMonth)) {
                age--
            }

            age
        } catch (e: Exception) {
            0
        }
    }
}