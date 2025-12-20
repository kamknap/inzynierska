package pl.fithubapp

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.fithubapp.data.CreateWeightMeasurementDto
import pl.fithubapp.data.UpdateProfileData
import pl.fithubapp.data.UpdateUserDto
import java.time.Instant
import kotlin.math.abs

object WeightSyncHelper {
    suspend fun getLatestWeight(context: Context): WeightRecord? {
        val client = HealthConnectClient.getOrCreate(context)

        val startTime = Instant.now().minusSeconds(30 * 24 * 60 * 60)
        val endTime = Instant.now()

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = WeightRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                    ascendingOrder = false,
                    pageSize = 1
                )
            )
            response.records.firstOrNull()
        } catch (e: Exception) {
            Log.e("WeightSync", "Błąd odczytu z Health Connect: ${e.message}")
            null
        }
    }

    suspend fun syncWeightToDatabase(context: Context, weightRecord: WeightRecord): Result<String> = withContext(Dispatchers.IO) {
        try {
            val weightInKg = weightRecord.weight.inKilograms
            val measuredAt = weightRecord.time.toString()

            val user = NetworkModule.api.getCurrentUser()
            val currentWeight = user.profile.weightKg

            if (abs(weightInKg - currentWeight) < 0.01) {
                Log.d("WeightSync", "Waga pobrana ($weightInKg) jest taka sama jak obecna - pomijam zapis.")
                return@withContext Result.success("Pominięto zapis - brak zmiany wagi (${weightInKg} kg)")
            }

            val historyDto = CreateWeightMeasurementDto(
                userId = user.id,
                weightKg = weightInKg,
                measuredAt = measuredAt
            )
            NetworkModule.api.createWeightMeasurement(historyDto)

            val updateUserDto = UpdateUserDto(
                profile = UpdateProfileData(
                    sex = user.profile.sex,
                    birthDate = user.profile.birthDate,
                    heightCm = user.profile.heightCm,
                    weightKg = weightInKg
                )
            )
            NetworkModule.api.updateUser(updateUserDto)

            Result.success("Zsynchronizowano nową wagę: ${String.format("%.1f", weightInKg)} kg")
        } catch (e: Exception) {
            Log.e("WeightSync", "Błąd synchronizacji z bazą: ${e.message}")
            Result.failure(e)
        }
    }
}