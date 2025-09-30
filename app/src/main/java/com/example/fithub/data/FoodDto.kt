package com.example.fithub.data

import com.google.gson.annotations.SerializedName

// Główny DTO dla produktu spożywczego
data class FoodDto(
    @SerializedName("_id") val id: String,
    val name: String,
    val brand: String?,
    val barcode: String?,
    val nutritionPer100g: NutritionData,
    val category: String,
    val verified: Boolean,
    val addedBy: String?,
    val createdAt: String,
    val updatedAt: String
)

// DTO dla wartości odżywczych na 100g
data class NutritionData(
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val fiber: Double,
    val sugar: Double,
    val sodium: Double
)

// DTO do tworzenia nowego produktu
data class CreateFoodDto(
    val name: String,
    val brand: String?,
    val barcode: String?,
    val nutritionPer100g: NutritionData,
    val category: String,
    val addedBy: String?
)

// DTO dla odpowiedzi z listą produktów
data class FoodsResponse(
    val foods: List<FoodDto>,
    val total: Int
)