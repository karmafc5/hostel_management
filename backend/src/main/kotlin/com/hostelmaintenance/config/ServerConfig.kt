package com.hostelmaintenance.config

data class ServerConfig(
    val port: Int,
    val mongoUri: String,
    val mongoDatabase: String,
    val jwtSecret: String,
    val jwtIssuer: String,
    val jwtAudience: String
) {
    companion object {
        fun fromEnvironment(environment: Map<String, String> = System.getenv()): ServerConfig {
            val port = environment["PORT"]?.let {
                it.toIntOrNull() ?: error("PORT must be a valid integer")
            } ?: 8080
            require(port in 1..65535) { "PORT must be between 1 and 65535" }

            val mongoUri = environment["MONGODB_URI"]
                ?.takeIf(String::isNotBlank)
                ?: error("MONGODB_URI must be configured")
            val jwtSecret = environment["JWT_SECRET"]
                ?.takeIf(String::isNotBlank)
                ?: error("JWT_SECRET must be configured")
            require(jwtSecret.toByteArray(Charsets.UTF_8).size >= 32) {
                "JWT_SECRET must contain at least 32 bytes"
            }

            return ServerConfig(
                port = port,
                mongoUri = mongoUri,
                mongoDatabase = environment["MONGODB_DATABASE"]
                    ?.takeIf(String::isNotBlank)
                    ?: "hostel_maintenance",
                jwtSecret = jwtSecret,
                jwtIssuer = environment["JWT_ISSUER"]
                    ?.takeIf(String::isNotBlank)
                    ?: "hostel-maintenance",
                jwtAudience = environment["JWT_AUDIENCE"]
                    ?.takeIf(String::isNotBlank)
                    ?: "hostel-maintenance-clients"
            )
        }
    }
}
