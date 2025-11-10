package com.example.fithub.data

import com.google.gson.annotations.SerializedName

data class ExerciseDto(
    @SerializedName("_id") val id: String,
    val name: String?,
    val muscleIds: List<String>?,
    val desc: String?,
    val instructions: List<String>?,
    val videoUrl: String?,
    @SerializedName("METS") val mets: Double?
    )
