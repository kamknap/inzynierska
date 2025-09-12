package com.example.fithub

import com.example.fithub.data.*
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body

interface ApiService {
    @GET("/api/users")
    suspend fun getUsers(): List<NewUserDto>

    @POST("/api/users")
    suspend fun createUser(@Body user: CreateUserDto): NewUserDto

    @POST("/api/user-goals")
    suspend fun createUserGoal(@Body userGoal: CreateUserGoalDto): UserGoalDto

    @GET("/api/user-goals")
    suspend fun getUserGoals(): List<UserGoalDto>
}