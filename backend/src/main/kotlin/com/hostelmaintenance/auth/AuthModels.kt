package com.hostelmaintenance.auth

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    STUDENT,
    STAFF,
    ADMIN
}

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val studentId: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class CreateStaffRequest(
    val email: String,
    val password: String,
    val fullName: String
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val fullName: String,
    val role: UserRole,
    val studentId: String? = null,
    val hostelId: String? = null,
    val roomNumber: String? = null
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresInSeconds: Long,
    val user: UserResponse
)

@Serializable
data class ApiError(val message: String)
