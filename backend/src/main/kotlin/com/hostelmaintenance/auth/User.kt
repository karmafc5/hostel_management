package com.hostelmaintenance.auth

data class User(
    val id: String,
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val role: UserRole,
    val studentId: String? = null,
    val hostelId: String? = null,
    val roomNumber: String? = null
) {
    fun toResponse() = UserResponse(
        id = id,
        email = email,
        fullName = fullName,
        role = role,
        studentId = studentId,
        hostelId = hostelId,
        roomNumber = roomNumber
    )
}
