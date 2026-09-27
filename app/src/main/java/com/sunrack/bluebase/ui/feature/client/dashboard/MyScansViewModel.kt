package com.sunrack.bluebase.ui.feature.client.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunrack.bluebase.data.api.OrdersApi
import com.sunrack.bluebase.data.api.WarrantyApi
import com.sunrack.bluebase.data.model.SavedOrder
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MyScansUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val scansByProjectThenKit: Map<String, Map<String, List<SavedOrder>>> = emptyMap(),
    val claimedTriples: Set<String> = emptySet(),
    val expandedProjects: Set<String> = emptySet(),
    val expandedKits: Set<String> = emptySet(),
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean get() = scansByProjectThenKit.isEmpty()
}

/** Ports `my-scans.tsx`'s project→kit accordion and its cross-reference against `/warranty-claims-status/`. */
class MyScansViewModel(
    private val ordersApi: OrdersApi,
    private val warrantyApi: WarrantyApi,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyScansUiState())
    val uiState: StateFlow<MyScansUiState> = _uiState.asStateFlow()

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
                val scansDeferred = async { ordersApi.savedOrders() }
                val claimsDeferred = async { warrantyApi.warrantyClaimsStatus() }
                val scans = scansDeferred.await()
                val claims = claimsDeferred.await()

                val claimedTriples = claims.mapNotNull { claim ->
                    val kitId = claim.kit_id?.trim()?.lowercase()
                    val orderId = claim.order?.order_id?.trim()?.lowercase()
                    val kitNo = claim.kit_number?.toString()
                    if (kitId != null && orderId != null && kitNo != null) "$orderId|$kitId|$kitNo" else null
                }.toSet()

                val grouped = scans.groupBy { it.project_id }
                    .mapValues { (_, projectScans) -> projectScans.groupBy { it.kit_id } }

                _uiState.value = _uiState.value.copy(
                    loading = false,
                    refreshing = false,
                    scansByProjectThenKit = grouped,
                    claimedTriples = claimedTriples,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    refreshing = false,
                    errorMessage = e.message ?: "Failed to load scans. Please check your connection.",
                )
            }
        }
    }

    fun isClaimed(scan: SavedOrder): Boolean {
        val orderId = scan.order_id?.trim()?.lowercase() ?: return false
        val kitId = scan.kit_id.trim().lowercase()
        val kitNo = scan.kit_no?.toString() ?: return false
        return "$orderId|$kitId|$kitNo" in _uiState.value.claimedTriples
    }

    fun toggleProject(projectId: String) {
        val current = _uiState.value.expandedProjects
        _uiState.value = _uiState.value.copy(
            expandedProjects = if (projectId in current) current - projectId else current + projectId,
        )
    }

    fun toggleKit(projectId: String, kitId: String) {
        val key = "$projectId|$kitId"
        val current = _uiState.value.expandedKits
        _uiState.value = _uiState.value.copy(
            expandedKits = if (key in current) current - key else current + key,
        )
    }
}
