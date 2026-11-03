package com.hostelmaintenance.auth

class AdminBootstrapper(
    private val users: UserRepository,
    private val passwordHasher: PasswordHasher
) {
    suspend fun bootstrap(environment: Map<String, String>) {
        val email = environment["BOOTSTRAP_ADMIN_EMAIL"]?.trim()
        val password = environment["BOOTSTRAP_ADMIN_PASSWORD"]
        if (email.isNullOrEmpty() && password.isNullOrEmpty()) return
        require(!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
            "BOOTSTRAP_ADMIN_EMAIL and BOOTSTRAP_ADMIN_PASSWORD must be set together"
        }
        require(AuthValidation.loginError(LoginRequest(email, password)) == null) {
            "Bootstrap administrator email is invalid"
        }
        require(password.length >= 12) {
            "BOOTSTRAP_ADMIN_PASSWORD must be at least 12 characters"
        }

        val normalizedEmail = AuthValidation.normalizedEmail(email)
        val existing = users.findByEmail(normalizedEmail)
        if (existing != null) {
            require(existing.role == UserRole.ADMIN) {
                "Bootstrap administrator email is already used by a non-admin account"
            }
            return
        }

        users.createAdmin(
            email = normalizedEmail,
            passwordHash = passwordHasher.hash(password),
            fullName = "System Administrator"
        )
    }
}
