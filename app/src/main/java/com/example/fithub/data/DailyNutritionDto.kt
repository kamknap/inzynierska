package com.example.fithub.data

import com.google.gson.annotations.SerializedName

// Główny DTO dla dziennika dziennego
data class DailyNutritionDto(
    @SerializedName("_id") val id: String?,
    val userId: String,
    val date: String, // format "2025-09-30"
    val meals: List<MealDto>,
    val dailyTotals: DailyTotalsDto,
    val calorieGoal: Int,
    val notes: String?,
    val createdAt: String?,
    val updatedAt: String?
)

// DTO dla posiłku
data class MealDto(
    val name: String, // "Śniadanie", "Lunch", "Kolacja", "Przekąska"
    val time: String, // "08:30"
    val foods: List<FoodItemDto>
)

// DTO dla produktu w posiłku (z ilością)
data class FoodItemDto(
    val foodId: String,
    val quantity: Int // w gramach
)

// DTO dla dziennych sum
data class DailyTotalsDto(
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double
)

// DTO do dodawania posiłku
data class AddMealDto(
    val meal: MealDto
)

// DTO dla odpowiedzi z populowanymi produktami (do wyświetlania)
data class DailyNutritionWithFoodsDto(
    @SerializedName("_id") val id: String?,
    val userId: String,
    val date: String,
    val meals: List<MealWithFoodsDto>,
    val dailyTotals: DailyTotalsDto,
    val calorieGoal: Int,
    val notes: String?
)

// Posiłek z populowanymi produktami
data class MealWithFoodsDto(
    val name: String,
    val time: String,
    val foods: List<FoodItemWithDetailsDto>
)

// Produkt w posiłku z pełnymi danymi
data class FoodItemWithDetailsDto(
    val foodId: FoodDto, // pełny obiekt produktu
    val quantity: Int
)