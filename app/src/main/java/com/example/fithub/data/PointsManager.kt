package com.example.fithub.data

import android.util.Log
import com.example.fithub.NetworkModule
import java.time.Instant
import java.time.LocalDate
import kotlin.math.abs

object PointsManager {
    private const val POINTS_LOGIN = 10
    private const val POINTS_MEAL = 5
    private const val POINTS_TRAINING = 5
    private const val POINTS_WEIGHT = 5
    private const val POINTS_STREAK_LOGIN = 5
    private const val POINTS_CHALLENGE = 50
    private const val POINTS_TRAINING_FULL = 15


    enum class ActionType {
        LOGIN,
        MEAL,
        TRAINING,
        WEIGHT,
        CHALLENGE,
        STREAK,
        TRAINING_FULL
    }

    fun calculateNextLevelPoints(level: Int): Int{
        return 100 + (level - 1) * 25
    }

    suspend fun addPoints(userId: String, action: ActionType, customStreak: Int? = null): Boolean {
        try {
            Log.d("PointsManager", "Rozpoczynam addPoints dla userId=$userId, action=$action")
            val currentProgress = NetworkModule.api.getUserProgress(userId)
            Log.d("PointsManager", "Pobrano currentProgress: level=${currentProgress.level}, currentPoints=${currentProgress.currentPoints}")

            val pointsToAdd = when (action) {
                ActionType.MEAL -> POINTS_MEAL
                ActionType.LOGIN -> POINTS_LOGIN
                ActionType.WEIGHT -> POINTS_WEIGHT
                ActionType.TRAINING -> POINTS_TRAINING
                ActionType.CHALLENGE -> POINTS_CHALLENGE
                ActionType.STREAK -> POINTS_STREAK_LOGIN
                ActionType.TRAINING_FULL -> POINTS_TRAINING_FULL
            }

            var newCurrentPoints = currentProgress.currentPoints + pointsToAdd
            var newTotalPoints = currentProgress.totalPoints + pointsToAdd
            var newPointsToNextLevel = currentProgress.pointsToNextLevel - pointsToAdd
            var newLevel = currentProgress.level

            while (newPointsToNextLevel <= 0) {
                newLevel++

                val overflow = abs(newPointsToNextLevel)

                val nextLevelRequirement = calculateNextLevelPoints(newLevel)

                newPointsToNextLevel = nextLevelRequirement - overflow

                newCurrentPoints = overflow
            }

            val hasLeveledUp = newLevel > currentProgress.level
            val finalStreak = customStreak?: currentProgress.loginStreak
            val newLastLoginDate = if (action == ActionType.LOGIN) getTodayIsoDate() else currentProgress.lastLoginDate

            val updatedProgress = currentProgress.copy(
                level = newLevel,
                currentPoints = newCurrentPoints,
                totalPoints = newTotalPoints,
                pointsToNextLevel = newPointsToNextLevel,
                lastLoginDate = newLastLoginDate,
                loginStreak = finalStreak
            )

            Log.d("PointsManager", "Wysyłam update do API dla userId=$userId")
            NetworkModule.api.updateUserProgress(userId, updatedProgress)
            Log.d("PointsManager", "Update wysłany pomyślnie")

            Log.d("PointsManager", "Przyznano $pointsToAdd pkt za $action. Streak: $finalStreak. Nowy level: $newLevel, punkty: $newCurrentPoints")

            return hasLeveledUp

        } catch (e: Exception) {
            Log.e("PointsManager", "Błąd addPoints: ${e.message}", e)
            return false
        }
    }

    suspend fun removePoints(userId: String, action: ActionType) {
        try {
            Log.d("PointsManager", "Rozpoczynam removePoints dla userId=$userId, action=$action")
            val currentProgress = NetworkModule.api.getUserProgress(userId)

            val pointsToRemove = when (action) {
                ActionType.MEAL -> POINTS_MEAL
                ActionType.LOGIN -> POINTS_LOGIN
                ActionType.WEIGHT -> POINTS_WEIGHT
                ActionType.TRAINING -> POINTS_TRAINING
                ActionType.CHALLENGE -> POINTS_CHALLENGE
                ActionType.STREAK -> POINTS_STREAK_LOGIN
                ActionType.TRAINING_FULL -> POINTS_TRAINING_FULL
            }

            var newCurrentPoints = currentProgress.currentPoints - pointsToRemove
            var newTotalPoints = currentProgress.totalPoints - pointsToRemove
            var newPointsToNextLevel = currentProgress.pointsToNextLevel + pointsToRemove
            var newLevel = currentProgress.level

            if (newTotalPoints < 0) newTotalPoints = 0

            while (newCurrentPoints < 0 && newLevel > 1) {
                newLevel--

                val prevLevelRequirement = calculateNextLevelPoints(newLevel)

                newCurrentPoints += prevLevelRequirement

                newPointsToNextLevel = prevLevelRequirement - newCurrentPoints
            }

            if (newLevel == 1 && newCurrentPoints < 0) {
                newCurrentPoints = 0
                newPointsToNextLevel = calculateNextLevelPoints(1)
            }

            val updatedProgress = currentProgress.copy(
                level = newLevel,
                currentPoints = newCurrentPoints,
                totalPoints = newTotalPoints,
                pointsToNextLevel = newPointsToNextLevel
            )

            Log.d("PointsManager", "Wysyłam update (remove) do API. Nowy level: $newLevel, punkty: $newCurrentPoints")
            NetworkModule.api.updateUserProgress(userId, updatedProgress)

        } catch (e: Exception) {
            Log.e("PointsManager", "Błąd removePoints: ${e.message}", e)
        }
    }

    suspend fun checkDailyLogin(userId: String): LoginResult {
        try {
            val progress = NetworkModule.api.getUserProgress(userId)
            val today = getTodaySimpleDate()
            val yesterday = getYesterdaySimpleDate()

            val lastLoginRaw = progress.lastLoginDate
            val lastLoginDay = if (!lastLoginRaw.isNullOrEmpty() && lastLoginRaw.length >= 10) {
                lastLoginRaw.substring(0, 10)
            } else {
                ""
            }

            if (lastLoginDay == today){
                Log.d("PointsManager", "Użytkownik już się dzisiaj logował ($today)")
                return LoginResult(false, 0, progress.loginStreak,
                    streakBonus = false,
                    levelUp = false
                )
            }

            var newStreak = 1
            var isStreakBonus = false
            var pointsEarned = POINTS_LOGIN

            if(lastLoginDay == yesterday){
                newStreak = progress.loginStreak + 1
                isStreakBonus = true
                pointsEarned += POINTS_STREAK_LOGIN
                Log.d("PointsManager", "Aktualizacja streak: $today")
                addPoints(userId, ActionType.STREAK)
            } else {
                Log.d("PointsManager", "Streak przerwany lub pierwsze logowanie. Reset do 1.")
            }

            addPoints(userId, ActionType.LOGIN, customStreak = newStreak)

            val updatedProgress = NetworkModule.api.getUserProgress(userId)
            val isLevelUp = updatedProgress.level > progress.level

            return LoginResult(
                isNewLogin = true,
                pointsAdded = pointsEarned,
                currentStreak = newStreak,
                streakBonus = isStreakBonus,
                levelUp = isLevelUp
            )

        } catch (e: Exception) {
            Log.e("PointsManager", "Błąd sprawdzania daily login: ${e.message}")
            return LoginResult(false, 0, 0, streakBonus = false, levelUp = false)
        }
    }

    private fun getTodayIsoDate(): String {
        return Instant.now().toString()
    }

    private fun getTodaySimpleDate(): String {
        return LocalDate.now().toString()
    }

    private fun getYesterdaySimpleDate(): String{
        return LocalDate.now().minusDays(1).toString()
    }

}