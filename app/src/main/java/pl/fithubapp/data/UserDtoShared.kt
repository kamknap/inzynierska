package pl.fithubapp.data

// Wspólne klasy używane w różnych DTO użytkownika

data class AuthData(
    val provider: String,
    val firebaseUid: String?
)

data class ProfileData(
    val sex: String,
    val birthDate: String,
    val heightCm: Int,
    val weightKg: Double
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
)

data class NotificationTypes(
    val workoutReminders: Boolean,
    val mealReminders: Boolean,
    val measureReminders: Boolean
)
