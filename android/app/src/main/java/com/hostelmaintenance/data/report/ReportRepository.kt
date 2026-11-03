package com.hostelmaintenance.data.report

import com.hostelmaintenance.data.remote.AuthApi

class ReportRepository(private val api: AuthApi) {
    suspend fun create(request: CreateReportRequestDto): MaintenanceReportDto = api.createReport(request)

    suspend fun listMine(): List<MaintenanceReportDto> = api.myReports()
}
