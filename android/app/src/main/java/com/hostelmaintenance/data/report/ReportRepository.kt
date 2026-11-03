package com.hostelmaintenance.data.report

import com.hostelmaintenance.data.remote.MaintenanceReportApi

class ReportRepository(private val api: MaintenanceReportApi) {
    suspend fun create(request: CreateReportRequestDto): MaintenanceReportDto = api.createReport(request)

    suspend fun listMine(): List<MaintenanceReportDto> = api.myReports()
}
