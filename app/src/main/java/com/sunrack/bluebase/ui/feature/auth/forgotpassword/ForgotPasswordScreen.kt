package com.sunrack.bluebase.ui.feature.auth.forgotpassword

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
fun ForgotPasswordScreen(
    onResetComplete: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: ForgotPasswordViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.resetSuccess) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = { TextButton(onClick = onResetComplete) { Text("OK") } },
            title = { Text("Success") },
            text = { Text("Password has been reset.") },
        )
    }

    ForgotPasswordContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onOtpChange = viewModel::onOtpChange,
        onNewPasswordChange = viewModel::onNewPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onSendOtp = viewModel::sendOtp,
        onVerifyOtp = viewModel::verifyOtp,
        onResetPassword = viewModel::resetPassword,
        onNavigateToLogin = onNavigateToLogin,
        modifier = modifier,
    )
}

@Composable
private fun ForgotPasswordContent(
    uiState: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    onVerifyOtp: () -> Unit,
    onResetPassword: () -> Unit,
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
                text = "Forgot Password",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp, top = 40.dp),
            )

            when (uiState.step) {
                ForgotPasswordStep.Email -> {
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = onEmailChange,
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    )
                    ActionButton("Send OTP", uiState.loading, onSendOtp)
                }

                ForgotPasswordStep.Otp -> {
                    OutlinedTextField(
                        value = uiState.otp,
                        onValueChange = onOtpChange,
                        label = { Text("OTP") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    )
                    ActionButton("Verify OTP", uiState.loading, onVerifyOtp)
                }

                ForgotPasswordStep.Reset -> {
                    PasswordField(
                        label = "New Password",
                        value = uiState.newPassword,
                        onValueChange = onNewPasswordChange,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    )
                    PasswordField(
                        label = "Confirm Password",
                        value = uiState.confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    )
                    ActionButton("Reset Password", uiState.loading, onResetPassword)
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

            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Go Back to Login")
            }
        }
    }
}

@Composable
private fun ActionButton(label: String, loading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !loading,
        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp))
        } else {
            Text(label)
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun ForgotPasswordScreenPreview() {
    BluebaseTheme {
        ForgotPasswordContent(
            uiState = ForgotPasswordUiState(),
            onEmailChange = {},
            onOtpChange = {},
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onSendOtp = {},
            onVerifyOtp = {},
            onResetPassword = {},
            onNavigateToLogin = {},
        )
    }
}
