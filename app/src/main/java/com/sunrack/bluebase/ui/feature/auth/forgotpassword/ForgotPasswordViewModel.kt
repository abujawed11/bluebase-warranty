package com.sunrack.bluebase.ui.feature.auth.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunrack.bluebase.core.network.apiErrorMessage
import com.sunrack.bluebase.data.api.AuthApi
import com.sunrack.bluebase.data.model.ResetPasswordRequest
import com.sunrack.bluebase.data.model.SendOtpRequest
import com.sunrack.bluebase.data.model.VerifyOtpRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ForgotPasswordStep { Email, Otp, Reset }

data class ForgotPasswordUiState(
    val email: String = "",
    val otp: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val step: ForgotPasswordStep = ForgotPasswordStep.Email,
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val resetSuccess: Boolean = false,
)

class ForgotPasswordViewModel(private val authApi: AuthApi) : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = update { it.copy(email = value) }
    fun onOtpChange(value: String) = update { it.copy(otp = value) }
    fun onNewPasswordChange(value: String) = update { it.copy(newPassword = value) }
    fun onConfirmPasswordChange(value: String) = update { it.copy(confirmPassword = value) }

    fun sendOtp() {
        update { it.copy(loading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                authApi.sendOtp(SendOtpRequest(email = _uiState.value.email, purpose = "reset"))
                update { it.copy(loading = false, step = ForgotPasswordStep.Otp) }
            } catch (e: Exception) {
                update { it.copy(loading = false, errorMessage = e.apiErrorMessage("Failed to send OTP. Please try again.")) }
            }
        }
    }

    fun verifyOtp() {
        update { it.copy(loading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                authApi.verifyOtp(VerifyOtpRequest(email = _uiState.value.email, otp = _uiState.value.otp))
                update { it.copy(loading = false, step = ForgotPasswordStep.Reset) }
            } catch (_: Exception) {
                update { it.copy(loading = false, errorMessage = "Invalid or expired OTP.") }
            }
        }
    }

    fun resetPassword() {
        val state = _uiState.value
        if (state.newPassword != state.confirmPassword) {
            update { it.copy(errorMessage = "Passwords do not match.") }
            return
        }
        update { it.copy(loading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                authApi.resetPassword(ResetPasswordRequest(email = state.email, password = state.newPassword))
                update { it.copy(loading = false, resetSuccess = true) }
            } catch (_: Exception) {
                update { it.copy(loading = false, errorMessage = "Failed to reset password.") }
            }
        }
    }

    private inline fun update(block: (ForgotPasswordUiState) -> ForgotPasswordUiState) {
        _uiState.value = block(_uiState.value)
    }
}
