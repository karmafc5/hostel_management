package com.hostelmaintenance.auth

import org.mindrot.jbcrypt.BCrypt

class PasswordHasher {
    fun hash(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt())

    fun verify(password: String, passwordHash: String): Boolean =
        BCrypt.checkpw(password, passwordHash)
}
