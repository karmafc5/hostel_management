package com.hostelmaintenance.auth

interface UserRepository {
    suspend fun createStudent(
        email: String,
        passwordHash: String,
        fullName: String,
        studentId: String
    ): User

    suspend fun createStaff(email: String, passwordHash: String, fullName: String): User

    suspend fun createAdmin(email: String, passwordHash: String, fullName: String): User

    suspend fun findByEmail(email: String): User?

    suspend fun findById(id: String): User?

    suspend fun listUsers(): List<User>
}

class DuplicateEmailException : RuntimeException("An account with this email already exists.")

class DuplicateStudentIdException : RuntimeException("This student ID is already registered.")
