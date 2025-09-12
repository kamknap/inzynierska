package com.example.fithub.data

data class UserSettings(
    val activityLevel: ActivityLevel,
    val notifications: NotificationsSettings = NotificationsSettings()
)
