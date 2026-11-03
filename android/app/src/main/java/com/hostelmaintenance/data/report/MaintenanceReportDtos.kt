package com.hostelmaintenance.data.report

data class CreateReportRequestDto(
    val title: String,
    val category: String,
    val description: String,
    val hostel: String,
    val room: String,
    val priority: String
)

data class MaintenanceReportDto(
    val id: String,
    val residentId: String,
    val title: String,
    val category: String,
    val description: String,
    val hostel: String,
    val room: String,
    val priority: String,
    val status: String,
    val createdAt: String,
    val isDemo: Boolean = false
)
