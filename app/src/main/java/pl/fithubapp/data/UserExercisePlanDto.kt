package pl.fithubapp.data

import com.google.gson.annotations.SerializedName

data class UserExercisePlanDto(
    @SerializedName("_id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("plan_name") val planName: String,
    @SerializedName("plan_exercises") val planExercises: List<PlanExerciseDto>,
    val createdAt: String,
    val updatedAt: String
)

data class PlanExerciseDto(
    @SerializedName("exercise_id") val exerciseId: ExerciseDto, // Wypełnione przez populate
    @SerializedName("_id") val id: String
)

data class CreateUserExercisePlanDto(
    @SerializedName("plan_name") val planName: String,
    @SerializedName("plan_exercises") val planExercises: List<PlanExerciseItemDto> = emptyList()
    // userId is now taken from Firebase Auth token, not from request body
)

data class PlanExerciseItemDto(
    @SerializedName("exercise_id") val exerciseId: String
)

data class UpdateUserExercisePlanDto(
    @SerializedName("plan_name") val planName: String? = null,
    @SerializedName("plan_exercises") val planExercises: List<PlanExerciseItemDto>? = null
)

data class AddExerciseToPlanDto(
    @SerializedName("exercise_id") val exerciseId: String
)

data class DeletePlanResponseDto(
    val message: String,
    val plan: UserExercisePlanDto
)