package com.sunrack.bluebase.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ReviewedBy(
    val username: String? = null,
)

/** `GET /warranty-claims-status-byid/{war_req_id}/` — the full claim shown by `warranty-status-page.tsx`. */
@Serializable
data class WarrantyClaimDetail(
    val war_req_id: String,
    val status: String = "pending",
    val status_updated_at: String? = null,
    val project_id: String? = null,
    val order: WarrantyClaimOrderRef? = null,
    val kit_number: String? = null,
    val kit_id: String? = null,
    val purchase_date: String? = null,
    val company_name: String? = null,
    val contact_name: String? = null,
    val contact_phone: String? = null,
    val email: String? = null,
    val accepted_statement: Boolean = false,
    val reviewed_by: ReviewedBy? = null,
    val review_comment: String? = null,
    // Dynamic {question: answer} map — answer can arrive as a bool, string ("yes"/"no"/"1"), etc;
    // normalized the same lenient way as `warranty-status-page.tsx`'s `normalizeToBool`.
    val checklist_answers: Map<String, JsonElement>? = null,
    val pdf_url: String? = null,
    val created_at: String? = null,
)

/** `GET /warranty-cards/my/` and `GET /warranty-cards/by-claim/{war_req_id}/` — ports `WarrantyCard.tsx`'s prop shape. */
@Serializable
data class WarrantyCardDetail(
    val war_card_id: String,
    val certificate_no: String,
    val warranty_type: String,
    val expires_at: String,
    val warranty_started_at: String,
    val warranty_duration_months: Int,
    val serial_number: String? = null,
    val coverage_description: String? = null,
    val is_transferable: Boolean = false,
    val issued_at: String,
    val terms_document_url: String? = null,
    val invoice_no: String? = null,
    val invoice_date: String? = null,
    val project_id: String? = null,
    val kit_id: String? = null,
    val client_id: String? = null,
    val company_name: String? = null,
    // Django DecimalFields serialize as JSON strings on this backend (see KitInfo's note); the RN
    // component defensively parses either shape, which is itself a signal these arrive as strings.
    val installation_latitude: String? = null,
    val installation_longitude: String? = null,
    val kit_no: String? = null,
)
