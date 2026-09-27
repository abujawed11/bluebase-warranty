package com.sunrack.bluebase.data.model

import kotlinx.serialization.Serializable

/**
 * The flat `/kits/` catalogue entry, grouped client-side by tilt angle + region (see `product-info.tsx`).
 * `clearance` is a Django `DecimalField`, serialized as a JSON string (see [KitInfo]'s note) — kept
 * as String here too rather than Double.
 */
@Serializable
data class ProductKit(
    val region: String? = null,
    val clearance: String? = null,
    val configuration: String? = null,
    val num_panels: Int = 0,
    val price: String? = null,
    val currency: String? = null,
    val tilt_angle: Int? = null,
)
