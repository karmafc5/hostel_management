package com.hostelmaintenance

import com.hostelmaintenance.auth.AuthService
import com.hostelmaintenance.auth.DuplicateEmailException
import com.hostelmaintenance.auth.DuplicateStudentIdException
import com.hostelmaintenance.auth.JwtTokenService
import com.hostelmaintenance.auth.PasswordHasher
import com.hostelmaintenance.auth.User
import com.hostelmaintenance.auth.UserRepository
import com.hostelmaintenance.auth.UserRole
import com.hostelmaintenance.auth.configureAuthentication
import com.hostelmaintenance.reports.CreateMaintenanceReportRequest
import com.hostelmaintenance.reports.MaintenanceReportPriority
import com.hostelmaintenance.reports.MaintenanceReportRepository
import com.hostelmaintenance.reports.MaintenanceReportResponse
import com.hostelmaintenance.reports.MaintenanceReportStatus
import com.hostelmaintenance.reports.configureMaintenanceReportRoutes
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.types.ObjectId
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MaintenanceReportRoutesTest {
    @Test
    fun residentCanCreateAndReadOnlyTheirOwnSavedReports() = testApplication {
        val users = TestUsers()
        val hasher = PasswordHasher()
        val resident = users.createStudent("resident@example.edu", hasher.hash("resident-password"), "Resident User", "STU-001")
        val otherResident = users.createStudent("other@example.edu", hasher.hash("other-password"), "Other Resident", "STU-002")
        val reports = TestReports()
        val tokens = tokens()

        application {
            module()
            configureAuthentication(AuthService(users, hasher, tokens), users, tokens)
            configureMaintenanceReportRoutes(reports)
        }

        assertEquals(HttpStatusCode.Unauthorized, client.get("/reports/me").status)

        val body = Json.encodeToString(
            CreateMaintenanceReportRequest(
                title = "Leaking bathroom tap",
                category = "Plumbing problems",
                description = "The bathroom sink tap has been leaking since this morning.",
                hostel = "Maple Hall",
                room = "B-204",
                priority = MaintenanceReportPriority.HIGH
            )
        )
        val createResponse = client.post("/reports") {
            header(HttpHeaders.Authorization, "Bearer ${tokens.responseFor(resident).accessToken}")
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        assertEquals(HttpStatusCode.Created, createResponse.status)
        assertTrue(createResponse.bodyAsText().contains("\"residentId\":\"${resident.id}\""))

        val residentReports = client.get("/reports/me") {
            header(HttpHeaders.Authorization, "Bearer ${tokens.responseFor(resident).accessToken}")
        }
        val otherReports = client.get("/reports/me") {
            header(HttpHeaders.Authorization, "Bearer ${tokens.responseFor(otherResident).accessToken}")
        }
        val residentRows = Json.decodeFromString(ListSerializer(MaintenanceReportResponse.serializer()), residentReports.bodyAsText())

        assertEquals(HttpStatusCode.OK, residentReports.status)
        assertEquals(1, residentRows.size)
        assertEquals("Leaking bathroom tap", residentRows.single().title)
        assertEquals(0, Json.decodeFromString(ListSerializer(MaintenanceReportResponse.serializer()), otherReports.bodyAsText()).size)
    }

    @Test
    fun invalidReportIsRejectedWithoutSaving() = testApplication {
        val users = TestUsers()
        val hasher = PasswordHasher()
        val resident = users.createStudent("resident@example.edu", hasher.hash("resident-password"), "Resident User", "STU-001")
        val reports = TestReports()
        val tokens = tokens()

        application {
            module()
            configureAuthentication(AuthService(users, hasher, tokens), users, tokens)
            configureMaintenanceReportRoutes(reports)
        }

        val response = client.post("/reports") {
            header(HttpHeaders.Authorization, "Bearer ${tokens.responseFor(resident).accessToken}")
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CreateMaintenanceReportRequest(
                        title = "x",
                        category = "Plumbing problems",
                        description = "short",
                        hostel = "Maple Hall",
                        room = "B-204"
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertFalse(response.bodyAsText().isBlank())
        assertTrue(reports.listForResident(resident.id).isEmpty())
    }

    private fun tokens() = JwtTokenService(
        secret = "local-test-secret-with-at-least-32-bytes",
        issuer = "test-issuer",
        audience = "test-audience"
    )

    private class TestUsers : UserRepository {
        private val users = mutableMapOf<String, User>()

        override suspend fun createStudent(email: String, passwordHash: String, fullName: String, studentId: String): User {
            if (users.values.any { it.email == email }) throw DuplicateEmailException()
            if (users.values.any { it.studentId == studentId }) throw DuplicateStudentIdException()
            return User(ObjectId().toHexString(), email, passwordHash, fullName, UserRole.STUDENT, studentId)
                .also { users[it.email] = it }
        }

        override suspend fun createStaff(email: String, passwordHash: String, fullName: String): User =
            createUser(email, passwordHash, fullName, UserRole.STAFF)

        override suspend fun createAdmin(email: String, passwordHash: String, fullName: String): User =
            createUser(email, passwordHash, fullName, UserRole.ADMIN)

        override suspend fun findByEmail(email: String): User? = users[email]
        override suspend fun findById(id: String): User? = users.values.firstOrNull { it.id == id }
        override suspend fun listUsers(): List<User> = users.values.toList()

        private fun createUser(email: String, hash: String, name: String, role: UserRole): User {
            if (users.values.any { it.email == email }) throw DuplicateEmailException()
            return User(ObjectId().toHexString(), email, hash, name, role).also { users[email] = it }
        }
    }

    private class TestReports : MaintenanceReportRepository {
        private val reports = mutableListOf<MaintenanceReportResponse>()

        override suspend fun create(residentId: String, request: CreateMaintenanceReportRequest): MaintenanceReportResponse =
            MaintenanceReportResponse(
                id = ObjectId().toHexString(),
                residentId = residentId,
                title = request.title.trim(),
                category = request.category.trim(),
                description = request.description.trim(),
                hostel = request.hostel.trim(),
                room = request.room.trim(),
                priority = request.priority,
                status = MaintenanceReportStatus.SUBMITTED,
                createdAt = Instant.now().toString()
            ).also { reports.add(it) }

        override suspend fun listForResident(residentId: String): List<MaintenanceReportResponse> =
            reports.filter { it.residentId == residentId }
    }
}
