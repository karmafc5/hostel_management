package com.hostelmaintenance.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.hostelmaintenance.data.auth.ApiErrorDto
import com.hostelmaintenance.data.auth.AuthRepository
import com.hostelmaintenance.data.auth.RegisterRequestDto
import com.hostelmaintenance.data.auth.UserDto
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(AuthUiState(isLoading = true))
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                if (!repository.hasStoredToken()) {
                    mutableState.update { it.copy(isLoading = false) }
                    return@launch
                }
                mutableState.update { it.copy(user = repository.currentUser(), isLoading = false) }
            } catch (exception: HttpException) {
                if (exception.code() == 401) repository.signOut()
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = if (exception.code() == 401) null else httpErrorMessage(exception)
                    )
                }
            } catch (exception: IOException) {
                mutableState.update {
                    it.copy(isLoading = false, errorMessage = "Unable to reach the server. Check your connection.")
                }
            } catch (exception: IllegalStateException) {
                mutableState.update {
                    it.copy(isLoading = false, errorMessage = exception.message ?: "Secure sign-in storage is unavailable.")
                }
            }
        }
    }

    fun toggleRegistrationMode() {
        mutableState.update { it.copy(isRegistrationMode = !it.isRegistrationMode, errorMessage = null) }
    }

    fun signIn(email: String, password: String) {
        val normalizedEmail = email.trim()
        if (normalizedEmail.isBlank() || password.isBlank()) {
            mutableState.update { it.copy(errorMessage = "Enter your email and password.") }
            return
        }
        authenticate { repository.login(normalizedEmail, password) }
    }

    fun register(
        fullName: String,
        studentId: String,
        email: String,
        password: String
    ) {
        if (
            fullName.isBlank() ||
            studentId.isBlank() ||
            email.isBlank() ||
            password.length < 8
        ) {
            mutableState.update {
                it.copy(errorMessage = "Complete the required fields. Passwords must be at least 8 characters.")
            }
            return
        }
        authenticate {
            repository.register(
                RegisterRequestDto(
                    email = email.trim(),
                    password = password,
                    fullName = fullName.trim(),
                    studentId = studentId.trim()
                )
            )
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                repository.signOut()
                mutableState.update { AuthUiState(isRegistrationMode = false) }
            } catch (exception: IllegalStateException) {
                mutableState.update {
                    it.copy(errorMessage = exception.message ?: "Unable to clear the saved session.")
                }
            }
        }
    }

    private fun authenticate(action: suspend () -> UserDto) {
        if (mutableState.value.isLoading) return
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                mutableState.update { it.copy(user = action(), isLoading = false) }
            } catch (exception: HttpException) {
                mutableState.update { it.copy(isLoading = false, errorMessage = httpErrorMessage(exception)) }
            } catch (exception: IOException) {
                mutableState.update {
                    it.copy(isLoading = false, errorMessage = "Unable to reach the server. Check your connection.")
                }
            } catch (exception: IllegalStateException) {
                mutableState.update {
                    it.copy(isLoading = false, errorMessage = exception.message ?: "Secure sign-in storage is unavailable.")
                }
            }
        }
    }

    private fun httpErrorMessage(exception: HttpException): String {
        val fallback = when (exception.code()) {
            401 -> "Email or password is incorrect."
            409 -> "An account with this email already exists."
            else -> "The server could not complete your request (${exception.code()})."
        }
        val body = exception.response()?.errorBody() ?: return fallback
        return try {
            Gson().fromJson(body.string(), ApiErrorDto::class.java)?.message ?: fallback
        } catch (exception: IOException) {
            fallback
        } catch (exception: JsonParseException) {
            fallback
        }
    }
}
