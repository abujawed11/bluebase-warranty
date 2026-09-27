package com.sunrack.bluebase.data.model

import kotlinx.serialization.Serializable

/** The flat `/kits/` catalogue entry, grouped client-side by tilt angle + region (see `product-info.tsx`). */
@Serializable
data class ProductKit(
    val region: String? = null,
    val clearance: Double? = null,
    val configuration: String? = null,
    val num_panels: Int = 0,
    val price: String? = null,
    val currency: String? = null,
    val tilt_angle: Int? = null,
)
