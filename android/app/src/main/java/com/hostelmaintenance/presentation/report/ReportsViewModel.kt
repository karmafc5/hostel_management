package com.hostelmaintenance.presentation.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelmaintenance.BuildConfig
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.hostelmaintenance.data.auth.ApiErrorDto
import com.hostelmaintenance.data.report.CreateReportRequestDto
import com.hostelmaintenance.data.report.DemoReportSamples
import com.hostelmaintenance.data.report.MaintenanceReportDto
import com.hostelmaintenance.data.report.ReportRepository
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class ReportsUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val reports: List<MaintenanceReportDto> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ReportsViewModel(private val repository: ReportRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(ReportsUiState())
    val state = mutableState.asStateFlow()

    fun load() {
        if (mutableState.value.isLoading) return
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val savedReports = repository.listMine()
                val visibleReports = if (BuildConfig.DEBUG && savedReports.isEmpty()) {
                    DemoReportSamples.create()
                } else {
                    savedReports
                }
                mutableState.update { it.copy(isLoading = false, reports = visibleReports) }
            } catch (exception: HttpException) {
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        reports = previewIfNeeded(it.reports),
                        errorMessage = httpError(exception)
                    )
                }
            } catch (exception: IOException) {
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        reports = previewIfNeeded(it.reports),
                        errorMessage = "Unable to reach the server. Showing sample preview data."
                    )
                }
            }
        }
    }

    fun submit(request: CreateReportRequestDto, onSuccess: () -> Unit) {
        if (mutableState.value.isSubmitting) return
        viewModelScope.launch {
            mutableState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
            try {
                val created = repository.create(request)
                mutableState.update {
                    it.copy(
                        isSubmitting = false,
                        reports = listOf(created) + it.reports.filterNot { report -> report.isDemo || report.id == created.id },
                        successMessage = "Report submitted and saved."
                    )
                }
                onSuccess()
            } catch (exception: HttpException) {
                mutableState.update { it.copy(isSubmitting = false, errorMessage = httpError(exception)) }
            } catch (exception: IOException) {
                mutableState.update { it.copy(isSubmitting = false, errorMessage = "Unable to reach the server. Your report was not saved.") }
            }
        }
    }

    fun clear() {
        mutableState.value = ReportsUiState()
    }

    private fun previewIfNeeded(reports: List<MaintenanceReportDto>): List<MaintenanceReportDto> =
        if (BuildConfig.DEBUG && reports.isEmpty()) DemoReportSamples.create() else reports

    private fun httpError(exception: HttpException): String {
        val fallback = when (exception.code()) {
            401 -> "Your session expired. Sign in again."
            403 -> "Only resident accounts can submit reports."
            400 -> "Check the report fields and try again."
            else -> "The server could not save the report (${exception.code()})."
        }
        val body = exception.response()?.errorBody() ?: return fallback
        return try {
            Gson().fromJson(body.string(), ApiErrorDto::class.java)?.message ?: fallback
        } catch (_: IOException) {
            fallback
        } catch (_: JsonParseException) {
            fallback
        }
    }
}
