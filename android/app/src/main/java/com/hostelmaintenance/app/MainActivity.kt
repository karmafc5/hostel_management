package com.hostelmaintenance.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.hostelmaintenance.di.AppContainer
import com.hostelmaintenance.presentation.auth.AuthViewModel
import com.hostelmaintenance.presentation.MaintenanceApp
import com.hostelmaintenance.presentation.theme.HostelMaintenanceTheme
import com.hostelmaintenance.presentation.report.ReportsViewModel

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        (application as HostelMaintenanceApplication).appContainer.authViewModelFactory
    }

    private val reportsViewModel: ReportsViewModel by viewModels {
        (application as HostelMaintenanceApplication).appContainer.reportsViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HostelMaintenanceTheme {
                MaintenanceApp(authViewModel, reportsViewModel)
            }
        }
    }
}
