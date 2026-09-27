package com.sunrack.bluebase.ui.feature.client.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunrack.bluebase.data.api.OrdersApi
import com.sunrack.bluebase.data.model.WarrantyDashboardCounts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardHomeUiState(
    val loading: Boolean = true,
    val counts: WarrantyDashboardCounts = WarrantyDashboardCounts(),
    val errorMessage: String? = null,
)

class DashboardHomeViewModel(private val ordersApi: OrdersApi) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardHomeUiState())
    val uiState: StateFlow<DashboardHomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = _uiState.value.copy(loading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val counts = ordersApi.warrantyDashboardCounts()
                _uiState.value = _uiState.value.copy(loading = false, counts = counts)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    errorMessage = e.message ?: "Failed to load warranty data",
                )
            }
        }
    }
}
