package com.hostelmaintenance.reports

import com.hostelmaintenance.auth.ApiError
import com.hostelmaintenance.auth.UserRole
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

fun Application.configureMaintenanceReportRoutes(repository: MaintenanceReportRepository) {
    routing {
        authenticate("auth-jwt") {
            post("/reports") {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()
                val residentId = principal?.payload?.getClaim("userId")?.asString()
                if (role != UserRole.STUDENT.name || residentId.isNullOrBlank()) {
                    call.respond(HttpStatusCode.Forbidden, ApiError("Only residents can submit maintenance reports."))
                    return@post
                }

                val request = call.receive<CreateMaintenanceReportRequest>()
                val validationError = MaintenanceReportValidation.error(request)
                if (validationError != null) {
                    call.respond(HttpStatusCode.BadRequest, ApiError(validationError))
                    return@post
                }

                call.respond(HttpStatusCode.Created, repository.create(residentId, request))
            }

            get("/reports/me") {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()
                val residentId = principal?.payload?.getClaim("userId")?.asString()
                if (role != UserRole.STUDENT.name || residentId.isNullOrBlank()) {
                    call.respond(HttpStatusCode.Forbidden, ApiError("Only residents can view maintenance reports."))
                    return@get
                }

                call.respond(HttpStatusCode.OK, repository.listForResident(residentId))
            }
        }
    }
}
