package com.example.fithub.data

data class LoginResult(
    val isNewLogin: Boolean,
    val pointsAdded: Int,
    val currentStreak: Int,
    val streakBonus: Boolean,
    val levelUp: Boolean
)
