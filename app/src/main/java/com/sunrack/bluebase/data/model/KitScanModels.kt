package com.sunrack.bluebase.data.model

import kotlinx.serialization.Serializable

/** `GET /kit-scan-details/{scan_id}/` — the shape shown by `kit-details.tsx` after a QR scan. */
@Serializable
data class KitScanDetailsResponse(
    val kit_id: String? = null,
    val prod_unit: String? = null,
    val warehouse: String? = null,
    val project_id: String? = null,
    val kit_no: String? = null,
    val date: String? = null,
    val order_id: String? = null,
    val kit: KitInfo? = null,
)

@Serializable
data class WarrantyClaimOrderRef(
    val order_id: String? = null,
)

/** Minimal shape of `GET /warranty-claims-status/` needed for kit-details' "already claimed" check; expanded in the warranty phase. */
@Serializable
data class WarrantyClaimStatusItem(
    val kit_id: String? = null,
    val kit_number: Int? = null,
    val order: WarrantyClaimOrderRef? = null,
    val war_req_id: String? = null,
)
