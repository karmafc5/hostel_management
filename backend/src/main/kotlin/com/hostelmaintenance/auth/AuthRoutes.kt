package com.hostelmaintenance.auth

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureAuthentication(
    authService: AuthService,
    users: UserRepository,
    tokens: JwtTokenService
) {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "hostel-maintenance"
            verifier(tokens.verifier)
            validate { credential ->
                val payload = credential.payload
                val userId = payload.getClaim("userId").asString()
                val role = payload.getClaim("role").asString()
                if (!userId.isNullOrBlank() && UserRole.entries.any { it.name == role }) {
                    JWTPrincipal(payload)
                } else {
                    null
                }
            }
        }
    }

    routing {
        authRoutes(authService)
        adminRoutes(authService, users)
        authenticate("auth-jwt") {
            get("/users/me") {
                val userId = call.principal<JWTPrincipal>()
                    ?.payload
                    ?.getClaim("userId")
                    ?.asString()
                val user = if (userId == null) null else users.findById(userId)
                if (user == null) {
                    call.respond(HttpStatusCode.NotFound, ApiError("Account not found."))
                } else {
                    call.respond(HttpStatusCode.OK, user.toResponse())
                }
            }
        }
    }
}

private fun Route.authRoutes(authService: AuthService) {
    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val validationError = AuthValidation.registrationError(request)
            if (validationError != null) {
                call.respond(HttpStatusCode.BadRequest, ApiError(validationError))
                return@post
            }

            try {
                call.respond(HttpStatusCode.Created, authService.register(request))
            } catch (exception: DuplicateStudentIdException) {
                call.respond(
                    HttpStatusCode.Conflict,
                    ApiError(exception.message ?: "Student ID is already registered.")
                )
            } catch (exception: DuplicateEmailException) {
                call.respond(
                    HttpStatusCode.Conflict,
                    ApiError(exception.message ?: "An account with this email already exists.")
                )
            }
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val validationError = AuthValidation.loginError(request)
            if (validationError != null) {
                call.respond(HttpStatusCode.BadRequest, ApiError(validationError))
                return@post
            }

            val response = authService.login(request)
            if (response == null) {
                call.respond(HttpStatusCode.Unauthorized, ApiError("Invalid email or password."))
            } else {
                call.respond(HttpStatusCode.OK, response)
            }
        }
    }
}

private fun Route.adminRoutes(authService: AuthService, users: UserRepository) {
    authenticate("auth-jwt") {
        get("/admin/users") {
            if (!call.requireRole(UserRole.ADMIN)) return@get
            call.respond(HttpStatusCode.OK, users.listUsers().map { it.toResponse() })
        }

        post("/admin/staff") {
            if (!call.requireRole(UserRole.ADMIN)) return@post
            val request = call.receive<CreateStaffRequest>()
            val validationError = AuthValidation.staffCreationError(request)
            if (validationError != null) {
                call.respond(HttpStatusCode.BadRequest, ApiError(validationError))
                return@post
            }

            try {
                call.respond(HttpStatusCode.Created, authService.createStaff(request))
            } catch (exception: DuplicateEmailException) {
                call.respond(
                    HttpStatusCode.Conflict,
                    ApiError(exception.message ?: "An account with this email already exists.")
                )
            }
        }
    }
}

private suspend fun ApplicationCall.requireRole(role: UserRole): Boolean {
    val actualRole = principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()
    if (actualRole == role.name) return true
    respond(
        HttpStatusCode.Forbidden,
        ApiError("You do not have permission to access this resource.")
    )
    return false
}
