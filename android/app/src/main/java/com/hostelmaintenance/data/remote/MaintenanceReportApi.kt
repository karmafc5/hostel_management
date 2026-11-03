package com.hostelmaintenance.data.remote

import com.hostelmaintenance.data.report.CreateReportRequestDto
import com.hostelmaintenance.data.report.MaintenanceReportDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MaintenanceReportApi {
    @POST("reports")
    suspend fun createReport(@Body request: CreateReportRequestDto): MaintenanceReportDto

    @GET("reports/me")
    suspend fun myReports(): List<MaintenanceReportDto>
}
