package com.example.fithub.data

import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar

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

    fun getBirthDateAsDate(): Date?{
        if (birthDate.isBlank()) return null
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dateFormat.parse(birthDate)
        }
        catch (e: Exception){
            null
        }
    }

    fun getBirthDateAsIsoString(): String? {
        val date = getBirthDateAsDate() ?: return null
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        } catch (e: Exception) {
            null
        }
    }

    fun getAge(): Int? {
        if (birthDate.isBlank()) return null
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val birth = dateFormat.parse(birthDate)
            if (birth != null) {
                val birthCalendar = Calendar.getInstance().apply { time = birth }
                val today = Calendar.getInstance()
                var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
                if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                    age--
                }
                age
            } else null
        } catch (e: Exception) {
            null
        }
    }

}