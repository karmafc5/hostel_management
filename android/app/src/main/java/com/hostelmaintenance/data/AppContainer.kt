package com.hostelmaintenance.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hostelmaintenance.BuildConfig
import com.hostelmaintenance.data.auth.AndroidTokenStore
import com.hostelmaintenance.data.auth.AuthRepository
import com.hostelmaintenance.network.AuthApi
import com.hostelmaintenance.network.AuthorizationInterceptor
import com.hostelmaintenance.presentation.auth.AuthViewModel
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

    val authViewModelFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (!modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                throw IllegalArgumentException("Unsupported ViewModel type: ${modelClass.name}")
            }
            return modelClass.cast(AuthViewModel(authRepository))
        }
    }
}
