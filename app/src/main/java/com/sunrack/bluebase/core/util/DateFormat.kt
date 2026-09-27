package com.sunrack.bluebase.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Ports `formatDate.ts`'s `formatDateTime`: "h:mm AM/PM | dd/MM/yyyy" in the device's local time zone. */
fun formatDateTime(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return "N/A"
    val date = parseIso(dateStr) ?: return "Invalid Date"
    val formatter = SimpleDateFormat("h:mm a '|' dd/MM/yyyy", Locale.US)
    return formatter.format(date)
}

/** Ports the plain `new Date(dateStr).toDateString()` calls used inline in several RN screens. */
fun formatDateOnly(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return "-"
    val date = parseIso(dateStr) ?: return "-"
    val formatter = SimpleDateFormat("EEE MMM dd yyyy", Locale.US)
    return formatter.format(date)
}

private fun parseIso(dateStr: String): Date? {
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd",
    )
    for (pattern in patterns) {
        try {
            val format = SimpleDateFormat(pattern, Locale.US)
            if (pattern.endsWith("'Z'")) format.timeZone = TimeZone.getTimeZone("UTC")
            return format.parse(dateStr)
        } catch (_: Exception) {
            // try next pattern
        }
    }
    return null
}
