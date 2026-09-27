package com.sunrack.bluebase.data.model

import kotlinx.serialization.Serializable

/** `GET /saved-orders/` — one row per QR scan, shown grouped by project then kit in `my-scans.tsx`. */
@Serializable
data class SavedOrder(
    val scan_id: String,
    val kit_id: String,
    val prod_unit: String? = null,
    val warehouse: String? = null,
    val project_id: String,
    val kit_no: Int? = null,
    val date: String? = null,
    val scanned_at: String? = null,
    val order_id: String? = null,
)
