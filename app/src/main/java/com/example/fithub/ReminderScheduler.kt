package com.example.fithub.logic

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun scheduleAllReminders(context: Context, userId: String,
                             workoutEnabled: Boolean,
                             mealEnabled: Boolean,
                             measureEnabled: Boolean) {

        val workManager = WorkManager.getInstance(context)

        // Anuluj stare, żeby nie dublować przy ponownym logowaniu/edycji
        workManager.cancelAllWorkByTag("FITHUB_REMINDER")

        if (measureEnabled) {
            scheduleDailyWork(context, userId, "WEIGHT", 7, 0)
        }

        if (workoutEnabled) {
            scheduleDailyWork(context, userId, "WORKOUT", 18, 0)
        }

        if (mealEnabled) {
            scheduleDailyWork(context, userId, "MEAL", 8, 0)
            scheduleDailyWork(context, userId, "MEAL", 12, 0)
            scheduleDailyWork(context, userId, "MEAL", 17, 0)
        }
    }

    private fun scheduleDailyWork(context: Context, userId: String, type: String, hour: Int, minute: Int) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        val initialDelay = target.timeInMillis - now.timeInMillis

        val data = Data.Builder()
            .putString("USER_ID", userId)
            .putString("TYPE", type)
            .build()

        val workRequest = PeriodicWorkRequest.Builder(
            ReminderWorker::class.java,
            24, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("FITHUB_REMINDER")
            .build()

        val uniqueName = "${type}_${hour}"

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            uniqueName,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}