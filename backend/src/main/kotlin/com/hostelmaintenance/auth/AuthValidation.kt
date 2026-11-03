package com.hostelmaintenance.auth

object AuthValidation {
    private val emailPattern = Regex("^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun normalizedEmail(email: String): String = email.trim().lowercase()

    fun registrationError(request: RegisterRequest): String? {
        accountError(request.email, request.password)?.let { return it }
        if (request.fullName.isBlank()) return "Full name is required."
        if (request.studentId.isBlank()) return "Student ID is required."
        return null
    }

    fun staffCreationError(request: CreateStaffRequest): String? {
        accountError(request.email, request.password)?.let { return it }
        if (request.fullName.isBlank()) return "Full name is required."
        return null
    }

    fun loginError(request: LoginRequest): String? {
        if (!emailPattern.matches(normalizedEmail(request.email))) return "Enter a valid email address."
        if (request.password.isBlank()) return "Password is required."
        return null
    }

    private fun accountError(email: String, password: String): String? {
        if (!emailPattern.matches(normalizedEmail(email))) return "Enter a valid email address."
        if (password.length < 8 || password.toByteArray(Charsets.UTF_8).size > 72) {
            return "Password must be 8 to 72 bytes long."
        }
        return null
    }
}
