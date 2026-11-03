package com.hostelmaintenance.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hostelmaintenance.BuildConfig
import com.hostelmaintenance.data.local.AndroidTokenStore
import com.hostelmaintenance.data.remote.AuthApi
import com.hostelmaintenance.data.remote.AuthorizationInterceptor
import com.hostelmaintenance.data.auth.AuthRepository
import com.hostelmaintenance.presentation.auth.AuthViewModel
import com.hostelmaintenance.data.report.ReportRepository
import com.hostelmaintenance.presentation.report.ReportsViewModel
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(context: Context) {
    private val tokenStore = AndroidTokenStore(context.applicationContext)
    private val authApi = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(
            OkHttpClient.Builder()
                .addInterceptor(AuthorizationInterceptor(tokenStore))
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AuthApi::class.java)

    private val authRepository = AuthRepository(authApi, tokenStore)
    private val reportRepository = ReportRepository(authApi)

    val authViewModelFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (!modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                throw IllegalArgumentException("Unsupported ViewModel type: ${modelClass.name}")
            }
            return modelClass.cast(AuthViewModel(authRepository))
        }
    }

    val reportsViewModelFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (!modelClass.isAssignableFrom(ReportsViewModel::class.java)) {
                throw IllegalArgumentException("Unsupported ViewModel type: ${modelClass.name}")
            }
            return modelClass.cast(ReportsViewModel(reportRepository))
        }
    }
}
