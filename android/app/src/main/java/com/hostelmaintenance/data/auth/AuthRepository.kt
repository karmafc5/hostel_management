package com.hostelmaintenance.data.auth

import com.hostelmaintenance.network.AuthApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val api: AuthApi,
    private val tokenStore: TokenStore
) {
    suspend fun register(request: RegisterRequestDto): UserDto {
        val response = api.register(request)
        withContext(Dispatchers.IO) { tokenStore.save(response.accessToken) }
        return response.user
    }

    suspend fun login(email: String, password: String): UserDto {
        val response = api.login(LoginRequestDto(email, password))
        withContext(Dispatchers.IO) { tokenStore.save(response.accessToken) }
        return response.user
    }

    suspend fun currentUser(): UserDto = api.currentUser()

    suspend fun signOut() {
        withContext(Dispatchers.IO) { tokenStore.clear() }
    }

    suspend fun hasStoredToken(): Boolean =
        withContext(Dispatchers.IO) { tokenStore.read() != null }
}
