package pl.fithubapp.data

import com.google.gson.annotations.SerializedName

data class PhotoDto(
    @SerializedName("_id") val id: String? = null,
    val photoUrl: String,
    val uploadedAt: String,
    val weightKg: Double
)
