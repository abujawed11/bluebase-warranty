package com.sunrack.bluebase.ui.feature.client.kitdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunrack.bluebase.data.api.OrdersApi
import com.sunrack.bluebase.data.api.WarrantyApi
import com.sunrack.bluebase.data.model.KitInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ClientKitDetailsUiState(
    val loading: Boolean = true,
    val errorMessage: String? = null,
    val kitId: String = "",
    val prodUnit: String = "",
    val warehouse: String = "",
    val projectId: String = "",
    val kitNo: String = "",
    val purchaseDate: String = "",
    val orderId: String = "",
    val kit: KitInfo? = null,
    val warReqId: String? = null,
) {
    val isClaimed: Boolean get() = warReqId != null
}

/**
 * Ports `kit-details.tsx`, including its cross-reference against `/warranty-claims-status/` to
 * detect whether this exact (order, kit, kit number) triple already has a claim.
 */
class ClientKitDetailsViewModel(
    private val ordersApi: OrdersApi,
    private val warrantyApi: WarrantyApi,
    private val kitId: String?,
    private val scanId: String?,
    private val allScanned: Boolean,
    private val totalKits: Int?,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ClientKitDetailsUiState())
    val uiState: StateFlow<ClientKitDetailsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = _uiState.value.copy(loading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val (base, kit) = when {
                    scanId != null -> {
                        val response = ordersApi.kitScanDetails(scanId)
                        BaseFields(
                            kitId = response.kit_id.orEmpty(),
                            prodUnit = response.prod_unit.orEmpty(),
                            warehouse = response.warehouse.orEmpty(),
                            projectId = response.project_id.orEmpty(),
                            kitNo = response.kit_no.orEmpty(),
                            purchaseDate = response.date.orEmpty(),
                            orderId = response.order_id.orEmpty(),
                        ) to response.kit
                    }
                    kitId != null -> {
                        val kitInfo = ordersApi.kitById(kitId)
                        BaseFields(kitId = kitId) to kitInfo
                    }
                    else -> throw IllegalStateException("Missing scan_id or kit_id")
                }

                val warReqId = findExistingClaim(base.orderId, base.kitId, base.kitNo)

                _uiState.value = _uiState.value.copy(
                    loading = false,
                    kitId = base.kitId,
                    prodUnit = base.prodUnit,
                    warehouse = base.warehouse,
                    projectId = base.projectId,
                    kitNo = base.kitNo,
                    purchaseDate = base.purchaseDate,
                    orderId = base.orderId,
                    kit = kit,
                    warReqId = warReqId,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    errorMessage = e.message ?: "Failed to load kit details",
                )
            }
        }
    }

    private suspend fun findExistingClaim(orderId: String, kitId: String, kitNo: String): String? {
        if (orderId.isBlank() || kitId.isBlank()) return null
        return runCatching {
            warrantyApi.warrantyClaimsStatus().firstOrNull { claim ->
                claim.kit_id?.trim()?.equals(kitId.trim(), ignoreCase = true) == true &&
                    claim.order?.order_id?.trim()?.equals(orderId.trim(), ignoreCase = true) == true &&
                    claim.kit_number?.toString() == kitNo.trim()
            }?.war_req_id
        }.getOrNull()
    }

    val allScannedInfo: Pair<Boolean, Int?> get() = allScanned to totalKits

    /** The claim button only shows up after a fresh scan (matches `kit-details.tsx`'s `scan_id &&` gate). */
    fun hasScanId(): Boolean = scanId != null

    private data class BaseFields(
        val kitId: String = "",
        val prodUnit: String = "",
        val warehouse: String = "",
        val projectId: String = "",
        val kitNo: String = "",
        val purchaseDate: String = "",
        val orderId: String = "",
    )
}
