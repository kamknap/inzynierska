package com.example.fithub

import android.os.Build
import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    private const val USE_AZURE = false

    private val BASE_URL = if (USE_AZURE) {
        "https://fithubapp-backend.calmriver-05379b6c.polandcentral.azurecontainerapps.io"
    } else {
        // Lokalny serwer
        if (isEmulator()) {
            "http://10.0.2.2:4000" // emulator
        } else {
            "http://192.168.1.28:4000" // fizyczny telefon
        }
    }

    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("NetworkModule", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val gson = GsonBuilder()
        .serializeNulls()
        .create()

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
    private val offClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "MyFitHub/1.0 (kamil.knapik@outlook.com)")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    // serwis dla wyszukiwania po nazwie (polska baza)
    val offApi: OpenFoodFactsService by lazy {
        Retrofit.Builder()
            .baseUrl("https://pl.openfoodfacts.org/")
            .client(offClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFoodFactsService::class.java)
    }

    // serwis do wyszukiwania po barcode (światowa baza)
    val offBarcodeApi: OpenFoodFactsService by lazy {
        Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/")
            .client(offClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFoodFactsService::class.java)
    }


}