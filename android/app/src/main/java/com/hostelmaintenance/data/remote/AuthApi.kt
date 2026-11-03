package com.hostelmaintenance.data.remote

import com.hostelmaintenance.data.auth.AuthResponseDto
import com.hostelmaintenance.data.auth.LoginRequestDto
import com.hostelmaintenance.data.auth.RegisterRequestDto
import com.hostelmaintenance.data.auth.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @GET("users/me")
    suspend fun currentUser(): UserDto
}
