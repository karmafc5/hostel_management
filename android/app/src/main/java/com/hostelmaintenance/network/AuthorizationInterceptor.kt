package com.hostelmaintenance.network

import com.hostelmaintenance.data.auth.TokenStore
import okhttp3.Interceptor
import okhttp3.Response

class AuthorizationInterceptor(
    private val tokenStore: TokenStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.url.encodedPath.startsWith("/auth/")) {
            return chain.proceed(request)
        }

        val token = tokenStore.read() ?: return chain.proceed(request)
        return chain.proceed(
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        )
    }
}
