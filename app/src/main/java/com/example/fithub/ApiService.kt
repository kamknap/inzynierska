package com.example.fithub

import com.example.fithub.data.*
import retrofit2.http.*


interface ApiService {
    @GET("/api/users")
    suspend fun getUsers(): List<NewUserDto>

    @POST("/api/users")
    suspend fun createUser(@Body user: CreateUserDto): NewUserDto

    @POST("/api/user-goals")
    suspend fun createUserGoal(@Body userGoal: CreateUserGoalDto): UserGoalDto

    @GET("/api/user-goals")
    suspend fun getUserGoals(): List<UserGoalDto>

    // Foods endpoints
    @GET("/api/foods")
    suspend fun getFoods(
        @Query("search") search: String? = null,
        @Query("category") category: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("skip") skip: Int = 0
    ): FoodsResponse

    @GET("/api/foods/{id}")
    suspend fun getFoodById(@Path("id") id: String): FoodDto

    @GET("/api/foods/barcode/{barcode}")
    suspend fun getFoodByBarcode(@Path("barcode") barcode: String): FoodDto

    @POST("/api/foods")
    suspend fun createFood(@Body food: CreateFoodDto): FoodDto

    @DELETE("/api/foods/{id}")
    suspend fun deleteFood(@Path("id") id: String)

    // Nutrition endpoints
    @GET("/api/nutrition/{userId}/{date}")
    suspend fun getDailyNutrition(
        @Path("userId") userId: String,
        @Path("date") date: String // format "2025-09-30"
    ): DailyNutritionWithFoodsDto

    @POST("/api/nutrition/{userId}/{date}/meal")
    suspend fun addMeal(
        @Path("userId") userId: String,
        @Path("date") date: String,
        @Body addMealDto: AddMealDto
    ): DailyNutritionWithFoodsDto

    @DELETE("/api/nutrition/{userId}/{date}/meal/{mealIndex}")
    suspend fun deleteMeal(
        @Path("userId") userId: String,
        @Path("date") date: String,
        @Path("mealIndex") mealIndex: Int
    ): DailyNutritionWithFoodsDto

    @DELETE("/api/nutrition/{userId}/{date}/meal/{mealIndex}/food/{foodIndex}")
    suspend fun deleteFoodFromMeal(
        @Path("userId") userId: String,
        @Path("date") date: String,
        @Path("mealIndex") mealIndex: Int,
        @Path("foodIndex") foodIndex: Int
    ): DailyNutritionWithFoodsDto

}