package com.example.fithub.logic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fithub.NetworkModule
import com.example.fithub.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userId = inputData.getString("USER_ID") ?: return Result.failure()
        val type = inputData.getString("TYPE") ?: return Result.failure()

        // Sprawdzamy warunki (czy dane już są w bazie)
        if (shouldSendNotification(userId, type)) {
            sendNotification(type)
        }

        return Result.success()
    }

    private suspend fun shouldSendNotification(userId: String, type: String): Boolean {
        return try {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

            when (type) {
                "WEIGHT" -> {
                    val history = NetworkModule.api.getUserWeightHistory(userId)
                    if (history.isEmpty()) return true
                    val lastDate = history.first().measuredAt.substring(0, 10)
                    return lastDate != today
                }
                "MEAL" -> {
                    val nutrition = NetworkModule.api.getDailyNutrition(userId, today)
                    return nutrition.meals.isEmpty()
                }
                "WORKOUT" -> {
                    return true
                }
                else -> false
            }
        } catch (e: Exception) {
            Log.e("ReminderWorker", "Błąd sprawdzania API: ${e.message}")
            false
        }
    }

    private fun sendNotification(type: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "fithub_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Przypomnienia FitHub", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val (title, content) = when (type) {
            "WEIGHT" -> "Czas na ważenie!" to "Nie zapomnij zaktualizować swojej wagi w FitHub."
            "MEAL" -> "Pora na posiłek" to "Pamiętaj, aby dodać swoje posiłki do dziennika."
            "WORKOUT" -> "Czas na trening!" to "Dziś jest dobry dzień, żeby się poruszać."
            else -> "Przypomnienie" to "Sprawdź FitHub!"
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round) // Upewnij się, że masz ikonę
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(type.hashCode(), notification)
    }
}