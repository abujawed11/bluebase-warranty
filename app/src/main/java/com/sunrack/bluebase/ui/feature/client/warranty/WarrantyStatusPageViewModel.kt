package com.sunrack.bluebase.ui.feature.client.warranty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunrack.bluebase.data.api.WarrantyApi
import com.sunrack.bluebase.data.model.WarrantyClaimDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull

data class WarrantyStatusPageUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val claim: WarrantyClaimDetail? = null,
    val errorMessage: String? = null,
)

/** Ports `warranty-status-page.tsx`. */
class WarrantyStatusPageViewModel(
    private val warrantyApi: WarrantyApi,
    private val warReqId: String,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WarrantyStatusPageUiState())
    val uiState: StateFlow<WarrantyStatusPageUiState> = _uiState.asStateFlow()

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
                val claim = warrantyApi.warrantyClaimStatusById(warReqId)
                _uiState.value = _uiState.value.copy(loading = false, refreshing = false, claim = claim)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    refreshing = false,
                    errorMessage = "Warranty request not found or network error.",
                )
            }
        }
    }
}

/** Ports `normalizeToBool`: tolerates a boolean, or a string like "yes"/"1"/"true". */
fun JsonElement?.normalizeToBool(): Boolean {
    val primitive = this as? JsonPrimitive ?: return false
    primitive.booleanOrNull?.let { return it }
    val text = primitive.contentOrNull?.trim()?.lowercase() ?: return false
    return text in setOf("true", "yes", "y", "1")
}
