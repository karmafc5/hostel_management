package com.hostelmaintenance.database

import com.hostelmaintenance.reports.CreateMaintenanceReportRequest
import com.hostelmaintenance.reports.MaintenanceReportPriority
import com.hostelmaintenance.reports.MaintenanceReportRepository
import com.hostelmaintenance.reports.MaintenanceReportResponse
import com.hostelmaintenance.reports.MaintenanceReportStatus
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.Document
import org.bson.types.ObjectId
import java.util.Date

class MongoMaintenanceReportRepository(
    private val reports: MongoCollection<Document>
) : MaintenanceReportRepository {
    suspend fun createIndexes() {
        reports.createIndex(
            Indexes.compoundIndex(Indexes.ascending("residentId"), Indexes.descending("createdAt")),
            IndexOptions().name("reports_resident_created")
        )
    }

    override suspend fun create(
        residentId: String,
        request: CreateMaintenanceReportRequest
    ): MaintenanceReportResponse {
        val now = Date()
        val document = Document("_id", ObjectId())
            .append("residentId", residentId)
            .append("title", request.title.trim())
            .append("category", request.category.trim())
            .append("description", request.description.trim())
            .append("hostel", request.hostel.trim())
            .append("room", request.room.trim())
            .append("priority", request.priority.name)
            .append("status", MaintenanceReportStatus.SUBMITTED.name)
            .append("createdAt", now)
            .append("isDemo", false)

        reports.insertOne(document)
        return document.toReport()
    }

    override suspend fun listForResident(residentId: String): List<MaintenanceReportResponse> =
        reports.find(Document("residentId", residentId))
            .sort(Sorts.descending("createdAt"))
            .toList()
            .map { it.toReport() }

    suspend fun seedDemoDataIfEmpty(residentId: String) {
        if (reports.find(Document("residentId", residentId)).limit(1).firstOrNull() != null) return

        val demoReports = listOf(
            CreateMaintenanceReportRequest(
                title = "Leaking tap in bathroom",
                category = "Plumbing problems",
                description = "The sink tap has been dripping continuously since yesterday.",
                hostel = "Maple Hall",
                room = "B-204",
                priority = MaintenanceReportPriority.NORMAL
            ) to MaintenanceReportStatus.IN_PROGRESS,
            CreateMaintenanceReportRequest(
                title = "Desk chair needs repair",
                category = "Broken furniture",
                description = "One wheel is loose and the chair is unstable when sitting.",
                hostel = "Maple Hall",
                room = "B-204",
                priority = MaintenanceReportPriority.LOW
            ) to MaintenanceReportStatus.SUBMITTED,
            CreateMaintenanceReportRequest(
                title = "Hallway light is out",
                category = "Electrical problems",
                description = "The ceiling light outside my room does not turn on.",
                hostel = "Maple Hall",
                room = "B-204",
                priority = MaintenanceReportPriority.HIGH
            ) to MaintenanceReportStatus.RESOLVED
        )
        val now = System.currentTimeMillis()
        demoReports.forEachIndexed { index, (request, status) ->
            reports.insertOne(
                Document("_id", ObjectId())
                    .append("residentId", residentId)
                    .append("title", request.title)
                    .append("category", request.category)
                    .append("description", request.description)
                    .append("hostel", request.hostel)
                    .append("room", request.room)
                    .append("priority", request.priority.name)
                    .append("status", status.name)
                    .append("createdAt", Date(now - (index * 86_400_000L)))
                    .append("isDemo", true)
            )
        }
    }

    private fun Document.toReport() = MaintenanceReportResponse(
        id = getObjectId("_id").toHexString(),
        residentId = getString("residentId"),
        title = getString("title"),
        category = getString("category"),
        description = getString("description"),
        hostel = getString("hostel"),
        room = getString("room"),
        priority = MaintenanceReportPriority.valueOf(getString("priority")),
        status = MaintenanceReportStatus.valueOf(getString("status")),
        createdAt = getDate("createdAt").toInstant().toString(),
        isDemo = getBoolean("isDemo", false)
    )
}
