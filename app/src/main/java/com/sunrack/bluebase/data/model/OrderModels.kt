package com.sunrack.bluebase.data.model

import kotlinx.serialization.Serializable

/** The backend's Kit serializer shape — used standalone (`/kit/{kit_id}/`) and nested in order items. */
@Serializable
data class KitInfo(
    val kit_id: String? = null,
    val tilt_angle: Double? = null,
    val clearance: Double? = null,
    val configuration: String? = null,
    val num_panels: Int? = null,
    val region: String? = null,
    val price: String? = null,
    val currency: String? = null,
)

@Serializable
data class OrderItem(
    val id: String,
    val kit: KitInfo,
    val quantity: Int,
    val unit_price: String? = null,
    val total_price: String? = null,
)

@Serializable
data class Order(
    val order_id: String,
    val client_id: String,
    val project_id: String,
    val order_date: String? = null,
    val status: String = "pending",
    val delivery_date: String? = null,
    val po_date: String? = null,
    val remarks: String? = null,
    val items: List<OrderItem> = emptyList(),
    val total_quantity: Int = 0,
    val kit_count: Int = 0,
    val delivery_address: String? = null,
    val billing_address: String? = null,
    val total_kits: String? = null,
    val delivery_status: String? = null,
)
