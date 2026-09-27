package com.sunrack.bluebase.core.util

import androidx.compose.ui.graphics.Color

/** Ports `statusColor.ts`'s `getStatusColor`, mapping order status text to a Tailwind-equivalent color. */
fun orderStatusColor(status: String?): Color = when (status?.lowercase()) {
    "pending" -> Color(0xFFEAB308) // yellow-500
    "processing" -> Color(0xFF60A5FA) // blue-400
    "shipped" -> Color(0xFFA855F7) // purple-500
    "delivered" -> Color(0xFF22C55E) // green-500
    "cancelled" -> Color(0xFFEF4444) // red-500
    else -> Color(0xFF6B7280) // gray-500
}
