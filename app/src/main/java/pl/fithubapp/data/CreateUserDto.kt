package pl.fithubapp.data

data class CreateUserDto(
    val username: String,
    val auth: AuthData,
    val profile: ProfileData,
    val computed: ComputedData? = null,
    val settings: SettingsData
)
