package com.sunrack.bluebase.ui.feature.client.productinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunrack.bluebase.data.api.OrdersApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductKitRow(
    val clearance: String,
    val configuration: String,
    val panels: String,
)

data class ProductKitGroup(
    val matrixLabel: String,
    val region: String,
    val rows: List<ProductKitRow>,
)

data class ProductInfoUiState(
    val loading: Boolean = true,
    val groups: List<ProductKitGroup> = emptyList(),
    val errorMessage: String? = null,
)

/** Ports `product-info.tsx`'s client-side grouping of the flat `/kits/` catalogue by tilt angle + region. */
class ProductInfoViewModel(private val ordersApi: OrdersApi) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductInfoUiState())
    val uiState: StateFlow<ProductInfoUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = _uiState.value.copy(loading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val kits = ordersApi.productKits()
                val grouped = LinkedHashMap<Pair<String, String>, MutableList<ProductKitRow>>()
                kits.forEach { kit ->
                    val matrixLabel = when (kit.tilt_angle) {
                        10 -> "Matrix A – 10° Tilt"
                        15 -> "Matrix B – 15° Tilt"
                        else -> "Matrix – ${kit.tilt_angle ?: "?"}° Tilt"
                    }
                    val region = kit.region ?: "Other Regions"
                    val clearanceStr = "${kit.clearance ?: 0.0} ft"
                    val panelsStr = "${kit.num_panels} ${if (kit.num_panels > 1) "Panels" else "Panel"}"
                    grouped.getOrPut(matrixLabel to region) { mutableListOf() }
                        .add(ProductKitRow(clearanceStr, kit.configuration ?: "-", panelsStr))
                }
                val groups = grouped.map { (key, rows) -> ProductKitGroup(key.first, key.second, rows) }
                _uiState.value = _uiState.value.copy(loading = false, groups = groups)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    errorMessage = e.message ?: "Failed to load product information",
                )
            }
        }
    }
}
