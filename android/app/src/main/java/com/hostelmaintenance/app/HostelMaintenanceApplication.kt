package com.hostelmaintenance.app

import android.app.Application
import com.hostelmaintenance.di.AppContainer

class HostelMaintenanceApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
