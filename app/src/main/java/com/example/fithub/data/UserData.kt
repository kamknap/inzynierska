package com.example.fithub.data

data class UserData(
    val name: String = "",
    val weight: Double? = null,
    val height: Double? = null,
    val age: Int? = null,
    val sex: String = ""
) {
    fun isValidForBMI(): Boolean = weight != null && height != null
    fun isValidForBMR(): Boolean = weight != null && height != null && age != null && sex.isNotBlank()
    fun isComplete(): Boolean = name.isNotBlank() && isValidForBMR()
}