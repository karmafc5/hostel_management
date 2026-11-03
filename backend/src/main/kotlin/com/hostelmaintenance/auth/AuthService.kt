package com.hostelmaintenance.auth

class AuthService(
    private val users: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val tokens: JwtTokenService
) {
    suspend fun register(request: RegisterRequest): AuthResponse {
        val email = AuthValidation.normalizedEmail(request.email)
        val user = users.createStudent(
            email = email,
            passwordHash = passwordHasher.hash(request.password),
            fullName = request.fullName.trim(),
            studentId = request.studentId.trim()
        )
        return tokens.responseFor(user)
    }

    suspend fun createStaff(request: CreateStaffRequest): UserResponse {
        val user = users.createStaff(
            email = AuthValidation.normalizedEmail(request.email),
            passwordHash = passwordHasher.hash(request.password),
            fullName = request.fullName.trim()
        )
        return user.toResponse()
    }

    suspend fun login(request: LoginRequest): AuthResponse? {
        val user = users.findByEmail(AuthValidation.normalizedEmail(request.email))
            ?: return null
        if (!passwordHasher.verify(request.password, user.passwordHash)) return null
        return tokens.responseFor(user)
    }
}
