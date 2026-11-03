package com.hostelmaintenance.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import com.hostelmaintenance.data.auth.UserDto
import com.hostelmaintenance.presentation.components.HostelFieldLabel
import com.hostelmaintenance.presentation.components.hostelTextFieldColors
import com.hostelmaintenance.presentation.components.hostelTextFieldTextStyle

@Composable
fun AuthScreen(
    state: AuthUiState,
    onSignIn: (email: String, password: String) -> Unit,
    onRegister: (fullName: String, studentId: String, email: String, password: String) -> Unit,
    onToggleMode: () -> Unit
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var studentId by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Build,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary
        )
        Text(
            "Hostel Maintenance",
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            if (state.isRegistrationMode) "Create your resident account" else "Sign in to manage your requests",
            modifier = Modifier.padding(top = 6.dp, bottom = 24.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (state.isRegistrationMode) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { HostelFieldLabel("Full name *") },
                textStyle = hostelTextFieldTextStyle(),
                colors = hostelTextFieldColors(),
                singleLine = true,
                enabled = !state.isLoading
            )
            OutlinedTextField(
                value = studentId,
                onValueChange = { studentId = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                label = { HostelFieldLabel("Student ID *") },
                textStyle = hostelTextFieldTextStyle(),
                colors = hostelTextFieldColors(),
                singleLine = true,
                enabled = !state.isLoading
            )
        }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            label = { HostelFieldLabel("University email *") },
            textStyle = hostelTextFieldTextStyle(),
            colors = hostelTextFieldColors(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            enabled = !state.isLoading
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            label = { HostelFieldLabel("Password *") },
            textStyle = hostelTextFieldTextStyle(),
            colors = hostelTextFieldColors(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            enabled = !state.isLoading
        )
        state.errorMessage?.let { message ->
            Text(
                text = message,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = {
                if (state.isRegistrationMode) {
                    onRegister(fullName, studentId, email, password)
                } else {
                    onSignIn(email, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            enabled = !state.isLoading,
            contentPadding = PaddingValues(vertical = 15.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Text(if (state.isRegistrationMode) "Create account" else "Sign in")
            }
        }
        TextButton(
            onClick = onToggleMode,
            enabled = !state.isLoading
        ) {
            Text(
                if (state.isRegistrationMode) "Already registered? Sign in"
                else "New resident? Create an account"
            )
        }
    }
}

@Composable
fun RoleNotAvailableScreen(
    user: UserDto,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome, ${user.fullName}", style = MaterialTheme.typography.headlineSmall)
        Text(
            "The ${user.role.lowercase()} workspace is being prepared.",
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyLarge
        )
        TextButton(onClick = onSignOut, modifier = Modifier.padding(top = 12.dp)) {
            Text("Sign out")
        }
    }
}
