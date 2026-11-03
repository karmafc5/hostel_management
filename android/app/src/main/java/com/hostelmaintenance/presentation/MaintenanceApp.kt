package com.hostelmaintenance.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hostelmaintenance.data.auth.UserDto
import com.hostelmaintenance.presentation.auth.AuthScreen
import com.hostelmaintenance.presentation.auth.AuthViewModel
import com.hostelmaintenance.presentation.auth.RoleNotAvailableScreen
import com.hostelmaintenance.presentation.home.ResidentScreen
import com.hostelmaintenance.presentation.report.ReportProblemScreen
import com.hostelmaintenance.presentation.report.ReportsUiState
import com.hostelmaintenance.presentation.report.ReportsViewModel
import com.hostelmaintenance.data.report.CreateReportRequestDto

private enum class ResidentTab(val label: String) {
    Home("Home"),
    Reports("My Reports"),
    Notifications("Notifications"),
    Profile("Profile")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceApp(authViewModel: AuthViewModel, reportsViewModel: ReportsViewModel) {
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    val reportsState by reportsViewModel.state.collectAsStateWithLifecycle()
    val user = authState.user

    LaunchedEffect(user?.id) {
        if (user == null) {
            reportsViewModel.clear()
        } else if (user.role == "STUDENT") {
            reportsViewModel.load()
        }
    }

    if (user == null) {
        AuthScreen(
            state = authState,
            onSignIn = authViewModel::signIn,
            onRegister = authViewModel::register,
            onToggleMode = authViewModel::toggleRegistrationMode
        )
        return
    }

    if (user.role != "STUDENT") {
        RoleNotAvailableScreen(user, authViewModel::signOut)
        return
    }

    ResidentApp(
        user = user,
        reportsState = reportsState,
        onRefreshReports = reportsViewModel::load,
        onSubmitReport = reportsViewModel::submit,
        onSignOut = {
            reportsViewModel.clear()
            authViewModel.signOut()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResidentApp(
    user: UserDto,
    reportsState: ReportsUiState,
    onRefreshReports: () -> Unit,
    onSubmitReport: (CreateReportRequestDto, () -> Unit) -> Unit,
    onSignOut: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(ResidentTab.Home) }
    var showingReportForm by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (showingReportForm) "Report a problem" else "Hostel Maintenance") },
                navigationIcon = {
                    if (showingReportForm) {
                        IconButton(onClick = { showingReportForm = false }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (!showingReportForm) {
                NavigationBar {
                    ResidentTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = when (tab) {
                                        ResidentTab.Home -> Icons.Filled.Home
                                        ResidentTab.Reports -> Icons.AutoMirrored.Filled.ReceiptLong
                                        ResidentTab.Notifications -> Icons.Filled.Notifications
                                        ResidentTab.Profile -> Icons.Filled.Person
                                    },
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { contentPadding ->
        if (showingReportForm) {
            ReportProblemScreen(
                contentPadding = contentPadding,
                state = reportsState,
                onSubmit = { request -> onSubmitReport(request) { showingReportForm = false } }
            )
        } else {
            ResidentScreen(
                tab = selectedTab.name,
                user = user,
                contentPadding = contentPadding,
                reports = reportsState,
                onRefreshReports = onRefreshReports,
                onReportProblem = { showingReportForm = true },
                onViewAllReports = { selectedTab = ResidentTab.Reports },
                onSignOut = onSignOut
            )
        }
    }
}
