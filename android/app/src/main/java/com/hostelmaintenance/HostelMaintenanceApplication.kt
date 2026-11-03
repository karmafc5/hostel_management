package com.hostelmaintenance

import android.app.Application
import com.hostelmaintenance.data.AppContainer

class HostelMaintenanceApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
