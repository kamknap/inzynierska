package com.example.fithub.data
import com.google.gson.annotations.SerializedName

data class UserProgressDto(
    @SerializedName("_id") val id: String? = null,
    val userId: String,
    val level: Int = 1,
    val currentPoints: Int = 0,
    val totalPoints: Int = 0,
    val pointsToNextLevel: Int = 100,
    val lastLoginDate: String? = null,
    val loginStreak: Int = 0,
    val badges: List<String> = emptyList(),
    val completedChallenges: List<String> = emptyList(),
    val activeChallenges: ActiveChallenge? = null,
    val statistics: Statistics? = null,
    val photos: List<PhotoReference> = emptyList()
)

data class ActiveChallenge(
    val challengeId: String,
    val counter: Int = 0,
    val totalToFinish: Int,
    val startedDate: String
)

data class Statistics(
    val totalMealsLogged: Int = 0,
    val totalExercisesCompleted: Int = 0,
    val totalWeightEntries: Int = 0,
    val longestStreak: Int = 0,
    val weightChange: Double = 0.0
)

data class PhotoReference(
    val photoId: String,
    val tag: String // "before" or "after"
)
