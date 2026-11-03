package com.hostelmaintenance.presentation.auth

import com.hostelmaintenance.data.auth.UserDto

data class AuthUiState(
    val isLoading: Boolean = false,
    val isRegistrationMode: Boolean = false,
    val user: UserDto? = null,
    val errorMessage: String? = null
)
