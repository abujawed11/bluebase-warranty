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

/**
 * `GET /warranty-claims-status/` — used both for kit-details' "already claimed" cross-reference
 * (Phase 4) and for the My Scans / Warranty Status list screens (Phase 6), which is why this
 * carries more fields than the claimed-check alone needs.
 */
@Serializable
data class WarrantyClaimStatusItem(
    val war_req_id: String? = null,
    val kit_id: String? = null,
    val kit_number: Int? = null,
    val order: WarrantyClaimOrderRef? = null,
    val project_id: String? = null,
    val status: String = "pending",
    val status_updated_at: String? = null,
    val review_comment: String? = null,
    val created_at: String? = null,
)
