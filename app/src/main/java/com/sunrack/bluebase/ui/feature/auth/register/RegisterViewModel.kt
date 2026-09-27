package com.sunrack.bluebase.ui.feature.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunrack.bluebase.core.network.apiErrorMessage
import com.sunrack.bluebase.data.api.AuthApi
import com.sunrack.bluebase.data.model.RegisterRequest
import com.sunrack.bluebase.data.model.SendOtpRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val clientId: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val otp: String = "",
    val otpSent: Boolean = false,
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val registrationSuccess: Boolean = false,
)

class RegisterViewModel(private val authApi: AuthApi) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onClientIdChange(value: String) = update { it.copy(clientId = value) }
    fun onUsernameChange(value: String) = update { it.copy(username = value) }
    fun onEmailChange(value: String) = update { it.copy(email = value) }
    fun onPasswordChange(value: String) = update { it.copy(password = value) }
    fun onConfirmPasswordChange(value: String) = update { it.copy(confirmPassword = value) }
    fun onOtpChange(value: String) = update { it.copy(otp = value) }

    fun changeEmail() = update { it.copy(otpSent = false, otp = "") }

    fun sendOtp() {
        val email = _uiState.value.email.trim()
        if (email.isEmpty()) {
            update { it.copy(errorMessage = "Please enter your email address") }
            return
        }
        update { it.copy(loading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                authApi.sendOtp(SendOtpRequest(email = email, purpose = "register"))
                update { it.copy(loading = false, otpSent = true) }
            } catch (e: Exception) {
                update { it.copy(loading = false, errorMessage = e.apiErrorMessage("Failed to send OTP.")) }
            }
        }
    }

    fun register() {
        val state = _uiState.value
        if (state.password != state.confirmPassword) {
            update { it.copy(errorMessage = "Passwords do not match") }
            return
        }
        update { it.copy(loading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                authApi.register(
                    RegisterRequest(
                        client_id = state.clientId,
                        username = state.username,
                        email = state.email,
                        password = state.password,
                        otp = state.otp,
                    )
                )
                update { it.copy(loading = false, registrationSuccess = true) }
            } catch (e: Exception) {
                update { it.copy(loading = false, errorMessage = e.apiErrorMessage("Registration failed.")) }
            }
        }
    }

    private inline fun update(block: (RegisterUiState) -> RegisterUiState) {
        _uiState.value = block(_uiState.value)
    }
}
