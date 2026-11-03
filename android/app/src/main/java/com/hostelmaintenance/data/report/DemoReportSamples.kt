package com.hostelmaintenance.data.report

import java.time.LocalDate

object DemoReportSamples {
    fun create(): List<MaintenanceReportDto> = listOf(
        MaintenanceReportDto(
            id = "demo-leaking-tap",
            residentId = "demo-resident",
            title = "Leaking tap in bathroom",
            category = "Plumbing problems",
            description = "The sink tap has been dripping continuously since yesterday.",
            hostel = "Maple Hall",
            room = "B-204",
            priority = "NORMAL",
            status = "IN_PROGRESS",
            createdAt = LocalDate.now().toString(),
            isDemo = true
        ),
        MaintenanceReportDto(
            id = "demo-chair-repair",
            residentId = "demo-resident",
            title = "Desk chair needs repair",
            category = "Broken furniture",
            description = "One wheel is loose and the chair is unstable when sitting.",
            hostel = "Maple Hall",
            room = "B-204",
            priority = "LOW",
            status = "SUBMITTED",
            createdAt = LocalDate.now().minusDays(1).toString(),
            isDemo = true
        ),
        MaintenanceReportDto(
            id = "demo-hallway-light",
            residentId = "demo-resident",
            title = "Hallway light is out",
            category = "Electrical problems",
            description = "The ceiling light outside my room does not turn on.",
            hostel = "Maple Hall",
            room = "B-204",
            priority = "HIGH",
            status = "RESOLVED",
            createdAt = LocalDate.now().minusDays(2).toString(),
            isDemo = true
        )
    )
}
