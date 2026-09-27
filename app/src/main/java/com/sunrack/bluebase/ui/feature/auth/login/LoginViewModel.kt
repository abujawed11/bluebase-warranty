package com.sunrack.bluebase.ui.feature.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunrack.bluebase.core.auth.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val errorMessage: String? = null,
)

class LoginViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(username = value)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }

    fun login() {
        val state = _uiState.value
        _uiState.value = state.copy(loading = true, errorMessage = null)
        viewModelScope.launch {
            val result = sessionManager.login(state.username, state.password)
            _uiState.value = _uiState.value.copy(
                loading = false,
                errorMessage = result.exceptionOrNull()?.message,
            )
            // On success, the navigation root observes SessionManager.user and redirects.
        }
    }
}
