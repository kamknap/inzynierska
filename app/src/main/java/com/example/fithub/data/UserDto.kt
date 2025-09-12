package com.example.fithub.data

data class UserDto(
    val _id: String,
    val username: String,
    val auth: AuthInfo,
    val profile: UserProfile,
    val computed: Computed? = null,
    val settings: UserSettings,
    val currentGoalId: String? = null,
    val createdAt: String,
    val updatedAt: String
)
