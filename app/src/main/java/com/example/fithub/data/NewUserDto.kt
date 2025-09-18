package com.example.fithub.data
import com.google.gson.annotations.SerializedName
import java.util.Date

data class NewUserDto(
    @SerializedName("_id") val id: String,
    val username: String,
    val auth: AuthData,
    val profile: ProfileData,
    val computed: ComputedData,
    val settings: SettingsData,
    val currentGoalId: String?,
    val createdAt: String,
    val updatedAt: String
)

data class AuthData(
    val provider: String,
    val firebaseUid: String?
)

data class ProfileData(
    val sex: String,
    val birthDate: String,
    val heightCm: Int,
    val weightKg: Int
)

data class ComputedData(
    val bmi: Double,
    val bmr: Double
)

data class SettingsData(
    val activityLevel: Int,
    val notifications: NotificationSettings,
    val preferredTrainingFrequencyPerWeek: Int
)

data class NotificationSettings(
    val enabled: Boolean,
    val types: NotificationTypes,
    val channels: NotificationChannels
)

data class NotificationTypes(
    val workoutReminders: Boolean,
    val mealReminders: Boolean,
    val measureReminders: Boolean
)

data class NotificationChannels(
    val push: Boolean,
    val email: Boolean
)
