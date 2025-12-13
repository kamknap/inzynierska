package pl.fithubapp.data
import com.google.gson.annotations.SerializedName

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
