package com.example.fithub.data

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

data class OnboardingUserData(
    val name: String = "",
    val weight: Double? = null,
    val height: Double? = null,
    val birthDate: String = "",
    val sex: String = "Male",
    val bmr: Double? = null,
    val bmi: Double? = null
) {
    fun isValidForBMI(): Boolean = weight != null && height != null
    fun isValidForBMR(): Boolean = weight != null && height != null && birthDate != "" && sex.isNotBlank()
    fun isComplete(): Boolean = name.isNotBlank() && isValidForBMR()

    fun getBirthDateAsDate(): LocalDate?{
        if (birthDate.isBlank()) return null
        return try {
            LocalDate.parse(birthDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        } catch (e: Exception) {
            null
        }
    }

    fun getBirthDateAsIsoString(): String? {
        return try {
            getBirthDateAsDate()?.format(DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            null
        }
    }

    fun getAge(): Int? {
        if (birthDate.isBlank()) return null
        return try {
            val birthLocalDate = LocalDate.parse(birthDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            Period.between(birthLocalDate, LocalDate.now()).years
        } catch (e: Exception) {
            null
        }
    }

}