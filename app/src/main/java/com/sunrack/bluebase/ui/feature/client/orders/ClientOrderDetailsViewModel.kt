package com.sunrack.bluebase.ui.feature.client.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunrack.bluebase.data.api.OrdersApi
import com.sunrack.bluebase.data.model.KitInfo
import com.sunrack.bluebase.data.model.Order
import com.sunrack.bluebase.data.model.OrderItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class KitGroup(
    val kitId: String,
    val kit: KitInfo,
    val totalQuantity: Int,
)

data class ClientOrderDetailsUiState(
    val loading: Boolean = true,
    val order: Order? = null,
    val kitGroups: List<KitGroup> = emptyList(),
    val expandedKitIds: Set<String> = emptySet(),
    val errorMessage: String? = null,
)

/** Ports `order-details.tsx`, including its client-side grouping of order items by kit. */
class ClientOrderDetailsViewModel(
    private val ordersApi: OrdersApi,
    private val orderId: String,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ClientOrderDetailsUiState())
    val uiState: StateFlow<ClientOrderDetailsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val order = ordersApi.orderDetails(orderId)
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    order = order,
                    kitGroups = groupByKit(order.items),
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    errorMessage = e.message ?: "Failed to load order details",
                )
            }
        }
    }

    fun toggleGroup(kitId: String) {
        val current = _uiState.value.expandedKitIds
        _uiState.value = _uiState.value.copy(
            expandedKitIds = if (kitId in current) current - kitId else current + kitId,
        )
    }

    private fun groupByKit(items: List<OrderItem>): List<KitGroup> {
        val byKitId = LinkedHashMap<String, Pair<KitInfo, Int>>()
        items.forEach { item ->
            val kitId = item.kit.kit_id ?: return@forEach
            val existing = byKitId[kitId]
            byKitId[kitId] = item.kit to ((existing?.second ?: 0) + item.quantity)
        }
        return byKitId.map { (kitId, pair) -> KitGroup(kitId, pair.first, pair.second) }
    }
}
