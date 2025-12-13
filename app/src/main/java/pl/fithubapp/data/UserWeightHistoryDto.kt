package pl.fithubapp.data

import com.google.gson.annotations.SerializedName

data class UserWeightHistoryDto(
    @SerializedName("_id") val id: String,
    val userId: String,
    val weightKg: Double,
    val measuredAt: String, // ISO date string
    val createdAt: String,
    val updatedAt: String
)

data class CreateWeightMeasurementDto(
    val userId: String,
    val weightKg: Double,
    val measuredAt: String // ISO date string, np. "2025-11-02T10:30:00.000Z"
)