package pl.fithubapp.data

import android.util.Log
import pl.fithubapp.NetworkModule

object ChallengeManager {

    var onChallengeCompleted: ((String, String, Int) -> Unit)? = null

    suspend fun checkChallengeProgress(action: ChallengeType, value: Double = 1.0) {
        try {
            val userProgress = NetworkModule.api.getUserProgress()
            val activeChallenge = userProgress.activeChallenges ?: return

            val allChallenges = NetworkModule.api.getAllChallenges()
            val challengeDef = allChallenges.find { it.id == activeChallenge.challengeId } ?: return

            if (challengeDef.type != action) return

            var newCounter = activeChallenge.counter
            var isCompleted = false

            when (action) {
                ChallengeType.STREAK -> {
                    newCounter = userProgress.loginStreak
                }
                ChallengeType.MEAL_COUNT -> {
                    newCounter += 1
                }
                ChallengeType.WEIGHT_LOSS -> {
                    newCounter += (value * 10).toInt()
                }
                ChallengeType.TRAINING_COUNT -> {
                    newCounter += 1
                }
                ChallengeType.TRAINING_PLAN_COUNT -> {
                    newCounter += 1
                }
            }

            if (newCounter >= activeChallenge.totalToFinish) {
                newCounter = activeChallenge.totalToFinish
                isCompleted = true
            }

            val updatedActiveChallenge = activeChallenge.copy(counter = newCounter)
            val progressToUpdate = userProgress.copy(activeChallenges = updatedActiveChallenge)

            NetworkModule.api.updateUserProgress(progressToUpdate)
            Log.d("ChallengeManager", "Zaktualizowano postęp wyzwania: $newCounter / ${activeChallenge.totalToFinish}")

            if (isCompleted) {
                completeChallenge(challengeDef, userProgress)
            }

        } catch (e: Exception) {
            Log.e("ChallengeManager", "Błąd aktualizacji wyzwania", e)
        }
    }

    private suspend fun completeChallenge(
        challengeDto: ChallengeDto,
        currentProgress: UserProgressDto
    ) {
        PointsManager.addPoints(PointsManager.ActionType.CHALLENGE, customPoints = challengeDto.pointsForComplete)

        // 2. WAŻNE: Pobierz ZAKTUALIZOWANY postęp z API, bo PointsManager właśnie zmienił level i punkty!
        val refreshedProgress = NetworkModule.api.getUserProgress()

        // 3. Teraz operuj na świeżych danych (refreshedProgress zamiast currentProgress)
        // Znajdź odznakę...
        val allBadges = NetworkModule.api.getAllBadges()
        val relatedBadge = allBadges.find { badge ->
            badge.name.equals(challengeDto.name, ignoreCase = true)
        }

        val newBadges = refreshedProgress.badges.toMutableList() // Używamy refreshedProgress
        if (relatedBadge != null && relatedBadge.id != null) {
            if (!newBadges.contains(relatedBadge.id)) {
                newBadges.add(relatedBadge.id)
            }
        }

        val newCompleted = refreshedProgress.completedChallenges.toMutableList() // Używamy refreshedProgress
        if (challengeDto.id != null && !newCompleted.contains(challengeDto.id)) {
            newCompleted.add(challengeDto.id)
        }

        // Zaktualizuj postęp użytkownika
        val finalProgress = currentProgress.copy(
            badges = newBadges,
            completedChallenges = newCompleted,
            activeChallenges = null, // Usuń aktywne wyzwanie
        )

        NetworkModule.api.updateUserProgress(finalProgress)

        onChallengeCompleted?.invoke(
            challengeDto.name,
            relatedBadge?.name ?: "Nieznana odznaka",
            challengeDto.pointsForComplete
        )

        Log.d("ChallengeManager", "✅ Wyzwanie '${challengeDto.name}' ukończone! Zdobyto ${challengeDto.pointsForComplete} pkt")
    }
}