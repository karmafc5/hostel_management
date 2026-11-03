package com.hostelmaintenance.data.auth

data class RegisterRequestDto(
    val email: String,
    val password: String,
    val fullName: String,
    val studentId: String
)

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class AuthResponseDto(
    val accessToken: String,
    val tokenType: String,
    val expiresInSeconds: Long,
    val user: UserDto
)

data class UserDto(
    val id: String,
    val email: String,
    val fullName: String,
    val role: String,
    val studentId: String?,
    val hostelId: String?,
    val roomNumber: String?
)

data class ApiErrorDto(val message: String)
