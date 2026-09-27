package com.sunrack.bluebase.ui.feature.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sunrack.bluebase.ui.components.PasswordField
import com.sunrack.bluebase.ui.theme.BluebaseTheme

@Composable
fun RegisterScreen(
    onRegistrationSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.registrationSuccess) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                TextButton(onClick = onRegistrationSuccess) { Text("Login Now") }
            },
            title = { Text("Success") },
            text = { Text("Registration successful!") },
        )
    }

    RegisterContent(
        uiState = uiState,
        onClientIdChange = viewModel::onClientIdChange,
        onUsernameChange = viewModel::onUsernameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onOtpChange = viewModel::onOtpChange,
        onChangeEmail = viewModel::changeEmail,
        onSendOtp = viewModel::sendOtp,
        onRegister = viewModel::register,
        onNavigateToLogin = onNavigateToLogin,
        modifier = modifier,
    )
}

@Composable
private fun RegisterContent(
    uiState: RegisterUiState,
    onClientIdChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onChangeEmail: () -> Unit,
    onSendOtp: () -> Unit,
    onRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            Text(
                text = "Register",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp, top = 40.dp),
            )

            OutlinedTextField(
                value = uiState.clientId,
                onValueChange = onClientIdChange,
                label = { Text("Client ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            )
            OutlinedTextField(
                value = uiState.username,
                onValueChange = onUsernameChange,
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            )
            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                enabled = !uiState.otpSent,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            )
            PasswordField(
                label = "Password",
                value = uiState.password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            )
            PasswordField(
                label = "Confirm Password",
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            )

            if (uiState.otpSent) {
                Text(
                    text = "OTP sent to ${uiState.email}",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                )
                TextButton(onClick = onChangeEmail) {
                    Text("Change email address")
                }
            }

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    textAlign = TextAlign.Center,
                )
            }

            if (!uiState.otpSent) {
                Button(
                    onClick = onSendOtp,
                    enabled = !uiState.loading,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                ) {
                    if (uiState.loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Send OTP")
                    }
                }
            } else {
                OutlinedTextField(
                    value = uiState.otp,
                    onValueChange = onOtpChange,
                    label = { Text("Enter OTP") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                )
                Button(
                    onClick = onRegister,
                    enabled = !uiState.loading,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                ) {
                    if (uiState.loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Register")
                    }
                }
            }

            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Already have an account? Login")
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun RegisterScreenPreview() {
    BluebaseTheme {
        RegisterContent(
            uiState = RegisterUiState(),
            onClientIdChange = {},
            onUsernameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onOtpChange = {},
            onChangeEmail = {},
            onSendOtp = {},
            onRegister = {},
            onNavigateToLogin = {},
        )
    }
}
