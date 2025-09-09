package com.example.fithub

import com.example.fithub.data.AddUserDto
import com.example.fithub.data.UserDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body

interface ApiService {
    @GET("/api/users")
    suspend fun getUsers(): List<UserDto>

    @POST("/api/users")
    suspend fun createUser(@Body user: AddUserDto): UserDto
}