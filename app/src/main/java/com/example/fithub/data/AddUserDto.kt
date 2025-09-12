package com.example.fithub.data

data class AddUserDto(
    val username: String,
    val auth: AuthInfo,
    val profile: UserProfile,
    val computed: Computed? = null,
    val settings: UserSettings
)
