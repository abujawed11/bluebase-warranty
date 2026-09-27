package com.sunrack.bluebase.ui.feature.client.warranty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunrack.bluebase.data.api.WarrantyApi
import com.sunrack.bluebase.data.model.WarrantyCardDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WarrantyCardDetailUiState(
    val loading: Boolean = true,
    val card: WarrantyCardDetail? = null,
    val errorMessage: String? = null,
)

/** Ports `warranty-card.tsx`. */
class WarrantyCardDetailViewModel(
    private val warrantyApi: WarrantyApi,
    private val warReqId: String,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WarrantyCardDetailUiState())
    val uiState: StateFlow<WarrantyCardDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val card = warrantyApi.warrantyCardByClaim(warReqId)
                _uiState.value = _uiState.value.copy(loading = false, card = card)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    errorMessage = "Card not found or not available.",
                )
            }
        }
    }
}
