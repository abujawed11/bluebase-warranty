package com.sunrack.bluebase.ui.feature.client.warranty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunrack.bluebase.data.api.WarrantyApi
import com.sunrack.bluebase.data.model.WarrantyCardDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MyWarrantyCardsUiState(
    val loading: Boolean = true,
    val cardsByProjectThenKit: Map<String, Map<String, List<WarrantyCardDetail>>> = emptyMap(),
    val expandedProjects: Set<String> = emptySet(),
    val expandedKits: Set<String> = emptySet(),
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean get() = cardsByProjectThenKit.isEmpty()
}

/** Ports `my-cards.tsx`'s project → kit grouping of `/warranty-cards/my/`. */
class MyWarrantyCardsViewModel(private val warrantyApi: WarrantyApi) : ViewModel() {
    private val _uiState = MutableStateFlow(MyWarrantyCardsUiState())
    val uiState: StateFlow<MyWarrantyCardsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = _uiState.value.copy(loading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val cards = warrantyApi.myWarrantyCards()
                val grouped = cards
                    .groupBy { it.project_id ?: "Unknown Project" }
                    .mapValues { (_, cardsForProject) ->
                        cardsForProject.groupBy { it.kit_id ?: it.serial_number ?: "Unknown Kit" }
                    }
                _uiState.value = _uiState.value.copy(loading = false, cardsByProjectThenKit = grouped)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    errorMessage = e.message ?: "Failed to load warranty cards",
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

    fun toggleKit(kitId: String) {
        val current = _uiState.value.expandedKits
        _uiState.value = _uiState.value.copy(
            expandedKits = if (kitId in current) current - kitId else current + kitId,
        )
    }
}
