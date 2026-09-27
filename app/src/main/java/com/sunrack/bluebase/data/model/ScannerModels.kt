package com.sunrack.bluebase.data.model

import kotlinx.serialization.Serializable

/** The JSON payload encoded in the QR itself, and also the exact body sent to `/save-order/`. */
@Serializable
data class QrPayload(
    val kit_id: String,
    val prod_unit: String,
    val warehouse: String,
    val project_id: String,
    val date: String,
)

/** Common response shape of both `/save-order/` and `/upload-qr/` (see `qr-scanner.tsx`). */
@Serializable
data class ScanResultResponse(
    val scan_id: String? = null,
    val all_scanned: Boolean? = null,
    val total_kits: Int? = null,
    val kit_id: String? = null,
)
