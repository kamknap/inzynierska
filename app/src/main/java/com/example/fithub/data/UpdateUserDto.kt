package com.example.fithub.data

data class UpdateUserDto(
    val profile: UpdateProfileData? = null,
    val settings: SettingsData? = null,
    val computed: ComputedData? = null
)

data class UpdateProfileData(
    val weightKg: Double? = null,
    val heightCm: Int? = null,
    val sex: String? = null,
    val birthDate: String? = null
)
