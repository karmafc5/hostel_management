package com.hostelmaintenance.reports

import kotlinx.serialization.Serializable

@Serializable
enum class MaintenanceReportPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}

@Serializable
enum class MaintenanceReportStatus {
    SUBMITTED,
    IN_PROGRESS,
    RESOLVED
}

@Serializable
data class CreateMaintenanceReportRequest(
    val title: String,
    val category: String,
    val description: String,
    val hostel: String,
    val room: String,
    val priority: MaintenanceReportPriority = MaintenanceReportPriority.NORMAL
)

@Serializable
data class MaintenanceReportResponse(
    val id: String,
    val residentId: String,
    val title: String,
    val category: String,
    val description: String,
    val hostel: String,
    val room: String,
    val priority: MaintenanceReportPriority,
    val status: MaintenanceReportStatus,
    val createdAt: String,
    val isDemo: Boolean = false
)

interface MaintenanceReportRepository {
    suspend fun create(
        residentId: String,
        request: CreateMaintenanceReportRequest
    ): MaintenanceReportResponse

    suspend fun listForResident(residentId: String): List<MaintenanceReportResponse>
}

object MaintenanceReportValidation {
    private val categories = setOf(
        "Plumbing problems",
        "Electrical problems",
        "Broken furniture",
        "Water supply problems",
        "Bathroom problems",
        "Door and window problems",
        "Internet and Wi-Fi problems",
        "Cleaning and sanitation",
        "Structural damage",
        "Other problems"
    )

    fun error(request: CreateMaintenanceReportRequest): String? = when {
        request.title.trim().length !in 3..120 -> "Title must be between 3 and 120 characters."
        request.category.trim() !in categories -> "Choose a valid maintenance category."
        request.description.trim().length !in 10..2000 -> "Description must be between 10 and 2,000 characters."
        request.hostel.trim().length !in 1..100 -> "Hostel is required."
        request.room.trim().length !in 1..30 -> "Room number is required."
        else -> null
    }
}
