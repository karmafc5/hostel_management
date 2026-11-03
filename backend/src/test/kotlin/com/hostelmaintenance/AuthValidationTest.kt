package com.hostelmaintenance

import com.hostelmaintenance.auth.AuthValidation
import com.hostelmaintenance.auth.RegisterRequest
import com.hostelmaintenance.config.ServerConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class AuthValidationTest {
    @Test
    fun registrationRequiresValidAccountFieldsAndStrongPassword() {
        val valid = RegisterRequest(
            email = "resident@example.edu",
            password = "strong-password-123",
            fullName = "Resident",
            studentId = "STU-001"
        )
        assertNull(AuthValidation.registrationError(valid))

        assertEquals(
            "Password must be 8 to 72 bytes long.",
            AuthValidation.registrationError(valid.copy(password = "short"))
        )
        assertEquals(
            "Enter a valid email address.",
            AuthValidation.registrationError(valid.copy(email = "not-an-email"))
        )
    }

    @Test
    fun serverRefusesMissingDatabaseAndWeakJwtSecrets() {
        assertFailsWith<IllegalStateException> {
            ServerConfig.fromEnvironment(emptyMap())
        }

        assertFailsWith<IllegalArgumentException> {
            ServerConfig.fromEnvironment(
                mapOf(
                    "MONGODB_URI" to "mongodb+srv://cluster.example",
                    "JWT_SECRET" to "too-short"
                )
            )
        }
    }
}
