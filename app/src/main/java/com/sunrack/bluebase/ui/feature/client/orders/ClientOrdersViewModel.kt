package com.sunrack.bluebase.ui.feature.client.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunrack.bluebase.data.api.OrdersApi
import com.sunrack.bluebase.data.model.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ClientOrdersUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val orders: List<Order> = emptyList(),
    val errorMessage: String? = null,
)

/** Ports `all-orders.tsx`. */
class ClientOrdersViewModel(private val ordersApi: OrdersApi) : ViewModel() {
    private val _uiState = MutableStateFlow(ClientOrdersUiState())
    val uiState: StateFlow<ClientOrdersUiState> = _uiState.asStateFlow()

    init {
        load(showRefreshing = false)
    }

    fun refresh() = load(showRefreshing = true)

    private fun load(showRefreshing: Boolean) {
        _uiState.value = _uiState.value.copy(
            loading = !showRefreshing,
            refreshing = showRefreshing,
            errorMessage = null,
        )
        viewModelScope.launch {
            try {
                val orders = ordersApi.orders()
                _uiState.value = _uiState.value.copy(loading = false, refreshing = false, orders = orders)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    refreshing = false,
                    errorMessage = e.message ?: "Failed to load orders. Please try again.",
                )
            }
        }
    }
}
