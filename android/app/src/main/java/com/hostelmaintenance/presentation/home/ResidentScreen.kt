package com.hostelmaintenance.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hostelmaintenance.data.auth.UserDto
import com.hostelmaintenance.data.report.MaintenanceReportDto
import com.hostelmaintenance.presentation.report.ReportsUiState

@Composable
fun ResidentScreen(
    tab: String,
    user: UserDto,
    contentPadding: PaddingValues,
    reports: ReportsUiState,
    onRefreshReports: () -> Unit,
    onReportProblem: () -> Unit,
    onViewAllReports: () -> Unit,
    onSignOut: () -> Unit
) {
    when (tab) {
        "Home" -> HomeDashboard(user, contentPadding, reports, onReportProblem, onViewAllReports)
        "My Reports" -> ReportsScreen(contentPadding, reports, onRefreshReports)
        "Notifications" -> EmptySection(
            contentPadding,
            "Notifications are coming soon",
            "Your saved maintenance requests are available under My Reports."
        )
        else -> ProfileSection(contentPadding, user, onSignOut)
    }
}

@Composable
private fun HomeDashboard(
    user: UserDto,
    contentPadding: PaddingValues,
    reports: ReportsUiState,
    onReportProblem: () -> Unit,
    onViewAllReports: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            end = 20.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Resident dashboard", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                Text(
                    "Welcome, ${user.fullName.substringBefore(' ')}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text("Your maintenance overview and recent activity.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        reports.successMessage?.let { message ->
            item { Text(message, color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodyMedium) }
        }
        item {
            Button(
                onClick = onReportProblem,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("  Report a problem", style = MaterialTheme.typography.titleMedium)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    title = "My reports",
                    value = reports.reports.size.toString()
                )
                DashboardCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Build,
                    title = "In progress",
                    value = reports.reports.count { it.status == "IN_PROGRESS" }.toString()
                )
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Filled.HomeWork, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Text("Hostel & room", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        user.roomNumber?.let { "Room $it" } ?: "Add your hostel and room when you report an issue.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent requests", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                OutlinedButton(onClick = onViewAllReports) { Text("View all") }
            }
        }
        if (reports.reports.isEmpty() && !reports.isLoading) {
            item { Text("Your submitted reports will appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(reports.reports.take(3), key = { it.id }) { report -> ReportCard(report) }
        }
        if (reports.errorMessage != null) {
            item { Text(reports.errorMessage, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun ReportsScreen(
    contentPadding: PaddingValues,
    state: ReportsUiState,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(contentPadding).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("My reports", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            OutlinedButton(onClick = onRefresh, enabled = !state.isLoading) { Text("Refresh") }
        }
        if (state.reports.any { it.isDemo }) {
            Text(
                "Sample preview data only. These examples are not saved; reports you submit are saved to your account.",
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.bodySmall
            )
        }
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (state.isLoading && state.reports.isEmpty()) {
            CircularProgressIndicator()
        } else if (state.reports.isEmpty()) {
            Text("No reports yet. Use Report a problem to submit your first request.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.reports, key = { it.id }) { report -> ReportCard(report) }
            }
        }
    }
}

@Composable
private fun ReportCard(report: MaintenanceReportDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(report.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(report.status.replace('_', ' '), color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelMedium)
            }
            Text("${report.category} · ${report.priority.lowercase().replaceFirstChar(Char::uppercase)} priority")
            Text("${report.hostel}, room ${report.room}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(report.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(report.createdAt.take(10), style = MaterialTheme.typography.labelSmall)
                if (report.isDemo) Text("Sample", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

@Composable
private fun DashboardCard(modifier: Modifier, icon: ImageVector, title: String, value: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProfileSection(contentPadding: PaddingValues, user: UserDto, onSignOut: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(contentPadding).padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Your profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Text(user.fullName, style = MaterialTheme.typography.titleLarge)
        Text(user.email, color = MaterialTheme.colorScheme.onSurfaceVariant)
        user.studentId?.let { Text("Student ID: $it") }
        Text("Role: ${user.role.lowercase().replaceFirstChar(Char::uppercase)}")
        OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) { Text("Sign out") }
    }
}

@Composable
private fun EmptySection(contentPadding: PaddingValues, title: String, message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(contentPadding).padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(message, modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
