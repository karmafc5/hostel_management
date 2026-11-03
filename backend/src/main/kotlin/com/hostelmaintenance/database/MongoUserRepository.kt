package com.hostelmaintenance.database

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.hostelmaintenance.auth.DuplicateEmailException
import com.hostelmaintenance.auth.DuplicateStudentIdException
import com.hostelmaintenance.auth.User
import com.hostelmaintenance.auth.UserRepository
import com.hostelmaintenance.auth.UserRole
import kotlinx.coroutines.flow.firstOrNull
import org.bson.Document
import org.bson.types.ObjectId
import com.mongodb.MongoWriteException
import java.util.Date
import kotlinx.coroutines.flow.toList

class MongoUserRepository(
    private val users: MongoCollection<Document>
) : UserRepository {
    suspend fun createIndexes() {
        users.createIndex(
            Indexes.ascending("email"),
            IndexOptions().unique(true).name("users_email_unique")
        )
        users.createIndex(
            Indexes.ascending("studentId"),
            IndexOptions().unique(true).sparse(true).name("users_student_id_unique")
        )
        users.createIndex(Indexes.ascending("role"), IndexOptions().name("users_role"))
    }

    override suspend fun createStudent(
        email: String,
        passwordHash: String,
        fullName: String,
        studentId: String
    ): User {
        return createUser(email, passwordHash, fullName, UserRole.STUDENT, studentId)
    }

    override suspend fun createStaff(email: String, passwordHash: String, fullName: String): User =
        createUser(email, passwordHash, fullName, UserRole.STAFF)

    override suspend fun createAdmin(email: String, passwordHash: String, fullName: String): User =
        createUser(email, passwordHash, fullName, UserRole.ADMIN)

    override suspend fun findByEmail(email: String): User? =
        users.find(Document("email", email)).firstOrNull()?.toUser()

    override suspend fun findById(id: String): User? {
        if (!ObjectId.isValid(id)) return null
        return users.find(Document("_id", ObjectId(id))).firstOrNull()?.toUser()
    }

    override suspend fun listUsers(): List<User> = users.find().toList().map { it.toUser() }

    private suspend fun createUser(
        email: String,
        passwordHash: String,
        fullName: String,
        role: UserRole,
        studentId: String? = null
    ): User {
        val document = Document("_id", ObjectId())
            .append("email", email)
            .append("passwordHash", passwordHash)
            .append("fullName", fullName)
            .append("role", role.name)
            .append("createdAt", Date())
        studentId?.let { document.append("studentId", it) }

        try {
            users.insertOne(document)
        } catch (exception: MongoWriteException) {
            if (exception.error.code == 11000) {
                if (exception.error.message?.contains("users_student_id_unique") == true) {
                    throw DuplicateStudentIdException()
                }
                throw DuplicateEmailException()
            }
            throw exception
        }
        return document.toUser()
    }

    private fun Document.toUser(): User = User(
        id = getObjectId("_id").toHexString(),
        email = getString("email"),
        passwordHash = getString("passwordHash"),
        fullName = getString("fullName"),
        role = UserRole.valueOf(getString("role")),
        studentId = getString("studentId"),
        hostelId = getString("hostelId"),
        roomNumber = getString("roomNumber")
    )
}
