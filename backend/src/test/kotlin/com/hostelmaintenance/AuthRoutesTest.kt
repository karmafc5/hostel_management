package com.hostelmaintenance

import com.hostelmaintenance.auth.AuthResponse
import com.hostelmaintenance.auth.AuthService
import com.hostelmaintenance.auth.JwtTokenService
import com.hostelmaintenance.auth.LoginRequest
import com.hostelmaintenance.auth.PasswordHasher
import com.hostelmaintenance.auth.RegisterRequest
import com.hostelmaintenance.auth.User
import com.hostelmaintenance.auth.UserRepository
import com.hostelmaintenance.auth.UserResponse
import com.hostelmaintenance.auth.UserRole
import com.hostelmaintenance.auth.configureAuthentication
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.types.ObjectId
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthRoutesTest {
    @Test
    fun residentCanRegisterLoginAndReadOwnProfile() = testApplication {
        val repository = TestUserRepository()
        val tokens = JwtTokenService(
            secret = "local-test-secret-with-at-least-32-bytes",
            issuer = "test-issuer",
            audience = "test-audience"
        )
        val service = AuthService(repository, PasswordHasher(), tokens)

        application {
            module()
            configureAuthentication(service, repository, tokens)
        }

        val registerResponse = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    RegisterRequest(
                        email = "  Student@Example.edu ",
                        password = "secure-pass-123",
                        fullName = "Test Student",
                        studentId = "STU-001"
                    )
                )
            )
        }
        assertEquals(HttpStatusCode.Created, registerResponse.status)
        val registration = Json.decodeFromString<AuthResponse>(registerResponse.bodyAsText())
        assertEquals(UserRole.STUDENT, registration.user.role)
        assertEquals("student@example.edu", registration.user.email)

        val loginResponse = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(LoginRequest("student@example.edu", "secure-pass-123")))
        }
        assertEquals(HttpStatusCode.OK, loginResponse.status)
        val login = Json.decodeFromString<AuthResponse>(loginResponse.bodyAsText())

        val profileResponse = client.get("/users/me") {
            header(HttpHeaders.Authorization, "Bearer ${login.accessToken}")
        }
        assertEquals(HttpStatusCode.OK, profileResponse.status)
        assertEquals("Test Student", Json.decodeFromString<UserResponse>(profileResponse.bodyAsText()).fullName)
    }

    @Test
    fun profileRequiresAuthentication() = testApplication {
        val repository = TestUserRepository()
        val tokens = JwtTokenService(
            secret = "local-test-secret-with-at-least-32-bytes",
            issuer = "test-issuer",
            audience = "test-audience"
        )
        application {
            module()
            configureAuthentication(AuthService(repository, PasswordHasher(), tokens), repository, tokens)
        }

        assertEquals(HttpStatusCode.Unauthorized, client.get("/users/me").status)
    }

    @Test
    fun studentCannotListUsersOrCreateStaff() = testApplication {
        val repository = TestUserRepository()
        val tokens = JwtTokenService(
            secret = "local-test-secret-with-at-least-32-bytes",
            issuer = "test-issuer",
            audience = "test-audience"
        )
        val service = AuthService(repository, PasswordHasher(), tokens)
        val resident = repository.createStudent(
            email = "student@example.edu",
            passwordHash = PasswordHasher().hash("resident-password"),
            fullName = "Test Student",
            studentId = "STU-001"
        )

        application {
            module()
            configureAuthentication(service, repository, tokens)
        }

        val authorization = "Bearer ${tokens.responseFor(resident).accessToken}"
        val listResponse = client.get("/admin/users") {
            header(HttpHeaders.Authorization, authorization)
        }
        val staffResponse = client.post("/admin/staff") {
            header(HttpHeaders.Authorization, authorization)
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(com.hostelmaintenance.auth.CreateStaffRequest(
                email = "staff@example.edu",
                password = "staff-password-123",
                fullName = "Maintenance Staff"
            )))
        }

        assertEquals(HttpStatusCode.Forbidden, listResponse.status)
        assertEquals(HttpStatusCode.Forbidden, staffResponse.status)
        assertEquals(1, repository.listUsers().size)
    }

    private class TestUserRepository : UserRepository {
        private val users = mutableMapOf<String, User>()

        override suspend fun createStudent(
            email: String,
            passwordHash: String,
            fullName: String,
            studentId: String
        ): User {
            if (email in users) throw com.hostelmaintenance.auth.DuplicateEmailException()
            val user = User(
                id = ObjectId().toHexString(),
                email = email,
                passwordHash = passwordHash,
                fullName = fullName,
                role = UserRole.STUDENT,
                studentId = studentId
            )
            users[email] = user
            return user
        }

        override suspend fun createStaff(email: String, passwordHash: String, fullName: String): User =
            createUser(email, passwordHash, fullName, UserRole.STAFF)

        override suspend fun createAdmin(email: String, passwordHash: String, fullName: String): User =
            createUser(email, passwordHash, fullName, UserRole.ADMIN)

        override suspend fun findByEmail(email: String): User? = users[email]

        override suspend fun findById(id: String): User? = users.values.firstOrNull { it.id == id }

        override suspend fun listUsers(): List<User> = users.values.toList()

        private fun createUser(email: String, passwordHash: String, fullName: String, role: UserRole): User {
            if (email in users) throw com.hostelmaintenance.auth.DuplicateEmailException()
            return User(
                id = ObjectId().toHexString(),
                email = email,
                passwordHash = passwordHash,
                fullName = fullName,
                role = role
            ).also { users[email] = it }
        }
    }
}
