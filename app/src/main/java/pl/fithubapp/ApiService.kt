package pl.fithubapp

import pl.fithubapp.data.*
import retrofit2.http.*


interface ApiService {
    @GET("/api/users")
    suspend fun getUsers(): List<NewUserDto>

    @GET("/api/users/{id}")
    suspend fun getUserById(@Path("id") id: String): NewUserDto

    @POST("/api/users")
    suspend fun createUser(@Body user: CreateUserDto): NewUserDto

    @POST("/api/user-goals")
    suspend fun createUserGoal(@Body userGoal: CreateUserGoalDto): UserGoalDto

    @GET("/api/user-goals/user/{userId}")
    suspend fun getUserGoalsByUserId(@Path("userId") userId: String): List<UserGoalDto>

    @PUT("/api/users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body updateData: UpdateUserDto
    ): NewUserDto

    @PUT("/api/user-goals/{id}")
    suspend fun updateUserGoal(
        @Path("id") id: String,
        @Body updateData: UpdateUserGoalDto
    ): UserGoalDto

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

    @DELETE("/api/nutrition/{userId}/{date}/food/{itemId}")
    suspend fun deleteFoodByItemId(
        @Path("userId") userId: String,
        @Path("date") date: String,
        @Path("itemId") itemId: String
    ): DailyNutritionWithFoodsDto

    @PUT("/api/nutrition/{userId}/{date}/food/{itemId}")
    suspend fun updateFoodQuantity(
        @Path("userId") userId: String,
        @Path("date") date: String,
        @Path("itemId") itemId: String,
        @Body request: UpdateFoodQuantityDto
    ): DailyNutritionWithFoodsDto

    // Weight History endpoints
    @GET("/api/weight-history/{userId}")
    suspend fun getUserWeightHistory(@Path("userId") userId: String): List<UserWeightHistoryDto>

    @POST("/api/weight-history")
    suspend fun createWeightMeasurement(@Body measurement: CreateWeightMeasurementDto): UserWeightHistoryDto

    //Exercises endpoints
    @GET("/api/exercises")
    suspend fun getExercisesByName(@Query("name") name: String): List<ExerciseDto>

    @GET("/api/exercises")
    suspend fun getExercisesByMuscleId(@Query("muscleId") name: String): List<ExerciseDto>

    // User Exercise Plans endpoints
    @GET("/api/user-exercise-plans")
    suspend fun getUserExercisePlans(@Query("user_id") userId: String): List<UserExercisePlanDto>

    @GET("/api/user-exercise-plans/{id}")
    suspend fun getUserExercisePlanById(@Path("id") id: String): UserExercisePlanDto

    @POST("/api/user-exercise-plans")
    suspend fun createUserExercisePlan(@Body plan: CreateUserExercisePlanDto): UserExercisePlanDto

    @PUT("/api/user-exercise-plans/{id}")
    suspend fun updateUserExercisePlan(
        @Path("id") id: String,
        @Body updateData: UpdateUserExercisePlanDto
    ): UserExercisePlanDto

    @DELETE("/api/user-exercise-plans/{id}")
    suspend fun deleteUserExercisePlan(@Path("id") id: String): DeletePlanResponseDto

    @POST("/api/user-exercise-plans/{id}/exercises")
    suspend fun addExerciseToPlan(
        @Path("id") planId: String,
        @Body exercise: AddExerciseToPlanDto
    ): UserExercisePlanDto

    @DELETE("/api/user-exercise-plans/{id}/exercises/{exercise_id}")
    suspend fun removeExerciseFromPlan(
        @Path("id") planId: String,
        @Path("exercise_id") exerciseId: String
    ): UserExercisePlanDto

    // User Progress endpoints
    @GET("/api/user-progress/{userId}")
    suspend fun getUserProgress(@Path("userId") userId: String): UserProgressDto

    @POST("/api/user-progress")
    suspend fun createUserProgress(@Body progress: UserProgressDto): UserProgressDto

    @PUT("/api/user-progress/{userId}")
    suspend fun updateUserProgress(
        @Path("userId") userId: String,
        @Body progress: UserProgressDto
    ): UserProgressDto

    @DELETE("/api/user-progress/{userId}")
    suspend fun deleteUserProgress(@Path("userId") userId: String): Unit

    // Photos endpoints
    @GET("/api/photos")
    suspend fun getAllPhotos(): List<PhotoDto>

    @GET("/api/photos/{id}")
    suspend fun getPhotoById(@Path("id") id: String): PhotoDto

    @POST("/api/photos")
    suspend fun addPhoto(@Body photo: PhotoDto): PhotoDto

    @PUT("/api/photos/{id}")
    suspend fun updatePhoto(@Path("id") id: String, @Body photo: PhotoDto): PhotoDto

    @DELETE("/api/photos/{id}")
    suspend fun deletePhoto(@Path("id") id: String): Unit

    // Badges endpoints
    @GET("/api/badges")
    suspend fun getAllBadges(): List<BadgeDto>

    // Challenges endpoints
    @GET("/api/challenges")
    suspend fun getAllChallenges(): List<ChallengeDto>
}