package com.hostelmaintenance

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.serialization.kotlinx.json.json
import com.hostelmaintenance.auth.AuthService
import com.hostelmaintenance.auth.AdminBootstrapper
import com.hostelmaintenance.auth.ApiError
import com.hostelmaintenance.auth.JwtTokenService
import com.hostelmaintenance.auth.PasswordHasher
import com.hostelmaintenance.auth.configureAuthentication
import com.hostelmaintenance.config.ServerConfig
import com.hostelmaintenance.database.MongoUserRepository
import com.hostelmaintenance.database.MongoMaintenanceReportRepository
import com.hostelmaintenance.auth.UserRole
import com.hostelmaintenance.reports.configureMaintenanceReportRoutes
import com.mongodb.kotlin.client.coroutine.MongoClient
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class HealthResponse(val status: String)

fun Application.module() {
    install(ContentNegotiation) {
        json(Json { prettyPrint = false })
    }
    install(StatusPages) {
        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ApiError("Request body is invalid."))
        }
    }

    routing {
        get("/health") {
            call.respond(HttpStatusCode.OK, HealthResponse(status = "ok"))
        }
    }
}

fun main() {
    val config = ServerConfig.fromEnvironment()
    val mongoClient = MongoClient.create(config.mongoUri)
    val mongoDatabase = mongoClient.getDatabase(config.mongoDatabase)
    val mongoUsers = MongoUserRepository(mongoDatabase.getCollection("users"))
    val mongoReports = MongoMaintenanceReportRepository(mongoDatabase.getCollection("maintenanceReports"))
    runBlocking {
        mongoUsers.createIndexes()
        mongoReports.createIndexes()
        AdminBootstrapper(mongoUsers, PasswordHasher()).bootstrap(System.getenv())
        System.getenv("MOCK_DATA_STUDENT_EMAIL")
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?.let { email ->
                val demoStudent = mongoUsers.findByEmail(email.lowercase())
                    ?: error("MOCK_DATA_STUDENT_EMAIL must match an existing resident account")
                require(demoStudent.role == UserRole.STUDENT) {
                    "MOCK_DATA_STUDENT_EMAIL must belong to a resident account"
                }
                mongoReports.seedDemoDataIfEmpty(demoStudent.id)
            }
    }

    val tokenService = JwtTokenService(
        secret = config.jwtSecret,
        issuer = config.jwtIssuer,
        audience = config.jwtAudience
    )
    val authService = AuthService(mongoUsers, PasswordHasher(), tokenService)

    val server = embeddedServer(Netty, port = config.port, host = "0.0.0.0") {
        module()
        configureAuthentication(authService, mongoUsers, tokenService)
        configureMaintenanceReportRoutes(mongoReports)
    }
    server.application.monitor.subscribe(ApplicationStopped) {
        mongoClient.close()
    }
    server.start(wait = true)
}
