package com.sunrack.bluebase.ui.feature.client.warranty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunrack.bluebase.data.api.WarrantyApi
import com.sunrack.bluebase.data.model.WarrantyClaimStatusItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ClaimStatusUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val claimsByProjectThenKit: Map<String, Map<String, List<WarrantyClaimStatusItem>>> = emptyMap(),
    val expandedProjects: Set<String> = emptySet(),
    val expandedKits: Set<String> = emptySet(),
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean get() = claimsByProjectThenKit.isEmpty()
}

/** Ports `warranty-status.tsx`'s project → kit accordion of `/warranty-claims-status/`. */
class ClaimStatusViewModel(private val warrantyApi: WarrantyApi) : ViewModel() {
    private val _uiState = MutableStateFlow(ClaimStatusUiState())
    val uiState: StateFlow<ClaimStatusUiState> = _uiState.asStateFlow()

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
                val claims = warrantyApi.warrantyClaimsStatus()
                val grouped = claims
                    .groupBy { it.project_id ?: it.order?.order_id ?: "Unknown" }
                    .mapValues { (_, claimsForProject) -> claimsForProject.groupBy { it.kit_id ?: "Unknown" } }
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    refreshing = false,
                    claimsByProjectThenKit = grouped,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    refreshing = false,
                    errorMessage = e.message ?: "Could not load warranty requests.",
                )
            }
        }
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
