package com.hostelmaintenance.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import java.time.Instant
import java.util.Date

class JwtTokenService(
    secret: String,
    private val issuer: String,
    private val audience: String,
    private val lifetimeSeconds: Long = 900
) {
    private val algorithm = Algorithm.HMAC256(secret)
    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    init {
        require(secret.toByteArray(Charsets.UTF_8).size >= 32) {
            "JWT secret must contain at least 32 bytes"
        }
        require(lifetimeSeconds > 0) { "JWT lifetime must be positive" }
    }

    fun responseFor(user: User): AuthResponse {
        val now = Instant.now()
        val token = JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(user.id)
            .withClaim("userId", user.id)
            .withClaim("role", user.role.name)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusSeconds(lifetimeSeconds)))
            .sign(algorithm)

        return AuthResponse(
            accessToken = token,
            expiresInSeconds = lifetimeSeconds,
            user = user.toResponse()
        )
    }
}
