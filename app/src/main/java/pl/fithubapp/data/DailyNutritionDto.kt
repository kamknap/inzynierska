package pl.fithubapp.data

import com.google.gson.annotations.SerializedName

// Główny DTO dla dziennego żywienia
data class DailyNutritionDto(
    @SerializedName("_id") val id: String,
    val userId: String,
    val date: String, // ISO date format
    val meals: List<MealDto>,
    val dailyTotals: DailyTotalsDto,
    val notes: String?,
    val createdAt: String,
    val updatedAt: String
)

// DTO dla posiłku
data class MealDto(
    val name: String,
    val foods: List<FoodItemDto>
)

// DTO dla pozycji żywności w posiłku
data class FoodItemDto(
    val foodId: String,
    val quantity: Double 
)

// DTO dla dziennych sum
data class DailyTotalsDto(
    val calorieGoal: Double,
    val calorieEaten: Double
)

// DTO z wypełnionymi danymi o produktach (do odpowiedzi GET)
data class DailyNutritionWithFoodsDto(
    @SerializedName("_id") val id: String,
    val userId: String,
    val date: String,
    val meals: List<MealWithFoodsDto>,
    val dailyTotals: DailyTotalsDto,
    val notes: String?,
    val createdAt: String,
    val updatedAt: String
)

// Meal z wypełnionymi danymi o produktach
data class MealWithFoodsDto(
    val name: String,
    val foods: List<FoodItemWithDetailsDto>
)

// FoodItem z pełnymi danymi o produkcie
data class FoodItemWithDetailsDto(
    val itemId: String,
    val foodId: FoodDto, // Pełny obiekt produktu
    val quantity: Double
)

// DTO do dodawania posiłku
data class AddMealDto(
    val meal: MealDto
)

data class UpdateFoodQuantityDto(
    val quantity: Double
)
