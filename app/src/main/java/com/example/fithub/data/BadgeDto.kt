package com.example.fithub.data

import com.google.gson.annotations.SerializedName

data class BadgeDto(
    @SerializedName("_id") val id: String? = null,
    val name: String,
    val desc: String,
    val iconUrl: String,
    val unlockedAt: String? = null
)
