package com.sunrack.bluebase.ui.feature.client.scanner

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunrack.bluebase.core.network.apiErrorMessage
import com.sunrack.bluebase.data.api.ScannerApi
import com.sunrack.bluebase.data.model.QrPayload
import com.sunrack.bluebase.data.model.ScanResultResponse
import com.sunrack.bluebase.ui.feature.client.ClientKitDetailsRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

data class QrScannerUiState(
    val scanned: Boolean = false,
    val torchEnabled: Boolean = false,
    val errorMessage: String? = null,
    val navigateTarget: ClientKitDetailsRoute? = null,
)

/** Ports `qr-scanner.tsx`'s scan handling, upload-from-gallery, and response routing. */
class QrScannerViewModel(private val scannerApi: ScannerApi) : ViewModel() {
    private val _uiState = MutableStateFlow(QrScannerUiState())
    val uiState: StateFlow<QrScannerUiState> = _uiState.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }

    /** Resets the scan lock and torch, matching the RN screen's `useFocusEffect` reset. */
    fun resetForFocus() {
        _uiState.value = _uiState.value.copy(scanned = false, torchEnabled = false)
    }

    fun toggleTorch() {
        _uiState.value = _uiState.value.copy(torchEnabled = !_uiState.value.torchEnabled)
    }

    fun consumeNavigation() {
        _uiState.value = _uiState.value.copy(navigateTarget = null)
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun onQrCodeDetected(raw: String) {
        if (_uiState.value.scanned) return
        _uiState.value = _uiState.value.copy(scanned = true)
        viewModelScope.launch {
            delay(900) // lets the user visually center the code, matching the RN screen's setTimeout
            try {
                val payload = json.decodeFromString(QrPayload.serializer(), raw)
                require(
                    payload.kit_id.isNotBlank() && payload.prod_unit.isNotBlank() &&
                        payload.warehouse.isNotBlank() && payload.project_id.isNotBlank() && payload.date.isNotBlank()
                ) { "Missing required fields in QR code." }
                val response = scannerApi.saveOrder(payload)
                handleResponse(response)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    scanned = false,
                    errorMessage = e.apiErrorMessage("Invalid QR code or server error."),
                )
            }
        }
    }

    fun uploadImage(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            try {
                val (bytes, mimeType) = withContext(Dispatchers.IO) {
                    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: error("Unable to read the selected file.")
                    bytes to (contentResolver.getType(uri) ?: "image/jpeg")
                }
                if (!mimeType.startsWith("image/")) {
                    _uiState.value = _uiState.value.copy(errorMessage = "Please select an image file.")
                    return@launch
                }
                val part = MultipartBody.Part.createFormData(
                    "file",
                    "qr_image.jpg",
                    bytes.toRequestBody(mimeType.toMediaType()),
                )
                val response = scannerApi.uploadQr(part)
                handleResponse(response)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    scanned = false,
                    errorMessage = e.apiErrorMessage("Invalid QR code or server error."),
                )
            }
        }
    }

    private fun handleResponse(response: ScanResultResponse) {
        val target = when {
            response.scan_id != null && response.all_scanned != true ->
                ClientKitDetailsRoute(scanId = response.scan_id, allScanned = false)
            response.scan_id != null && response.all_scanned == true ->
                ClientKitDetailsRoute(scanId = response.scan_id, allScanned = true, totalKits = response.total_kits)
            response.kit_id != null -> ClientKitDetailsRoute(kitId = response.kit_id)
            else -> null
        }
        _uiState.value = if (target == null) {
            _uiState.value.copy(scanned = false, errorMessage = "Unexpected server response.")
        } else {
            _uiState.value.copy(navigateTarget = target)
        }
    }
}
