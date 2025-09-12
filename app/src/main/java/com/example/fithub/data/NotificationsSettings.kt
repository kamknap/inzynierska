package com.example.fithub.data

data class NotificationsSettings(
    val types: List<NotificationTypes> = emptyList(),
    val channels: List<NotificationChannels> = emptyList()
)
