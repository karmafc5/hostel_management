package com.hostelmaintenance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.hostelmaintenance.presentation.auth.AuthViewModel
import com.hostelmaintenance.presentation.MaintenanceApp
import com.hostelmaintenance.presentation.theme.HostelMaintenanceTheme

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        (application as HostelMaintenanceApplication).appContainer.authViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HostelMaintenanceTheme {
                MaintenanceApp(authViewModel)
            }
        }
    }
}
