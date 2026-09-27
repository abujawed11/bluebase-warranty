package com.sunrack.bluebase.core.util

import java.util.Calendar
import java.util.Date
import java.util.Locale

private val ISO_PATTERN = Regex(
    """^(\d{4})-(\d{2})-(\d{2})(?:[T ](\d{2}):(\d{2})(?::(\d{2}))?)?"""
)

/**
 * Extracts the year/month/day(/time) fields directly from an ISO-ish date string via regex,
 * instead of handing the whole string to [java.text.SimpleDateFormat]. This backend emits dates
 * with 6-digit fractional seconds and a numeric UTC offset (e.g. `"2025-11-15T09:33:24.989899+05:30"`),
 * which no fixed `SimpleDateFormat` pattern matches — java.time would handle it cleanly but isn't
 * available below API 26 without desugaring, which isn't configured here. The timezone offset is
 * intentionally ignored; the numbers are rendered as wall-clock time, which is what `toDateString()`/
 * `toLocaleString()` effectively show in the RN app too.
 */
fun parseIsoDateOrNull(dateStr: String?): Date? {
    if (dateStr.isNullOrBlank()) return null
    val match = ISO_PATTERN.find(dateStr) ?: return null
    val groups = match.groupValues
    val calendar = Calendar.getInstance()
    calendar.clear()
    calendar.set(
        groups[1].toInt(),
        groups[2].toInt() - 1,
        groups[3].toInt(),
        groups[4].toIntOrNull() ?: 0,
        groups[5].toIntOrNull() ?: 0,
        groups[6].toIntOrNull() ?: 0,
    )
    return calendar.time
}

/** Ports `formatDate.ts`'s `formatDateTime`: "h:mm AM/PM | dd/MM/yyyy" in the device's local time zone. */
fun formatDateTime(dateStr: String?): String {
    val date = parseIsoDateOrNull(dateStr) ?: return if (dateStr.isNullOrBlank()) "N/A" else "Invalid Date"
    return java.text.SimpleDateFormat("h:mm a '|' dd/MM/yyyy", Locale.US).format(date)
}

/** Ports the plain `new Date(dateStr).toDateString()` calls used inline in several RN screens. */
fun formatDateOnly(dateStr: String?): String {
    val date = parseIsoDateOrNull(dateStr) ?: return "-"
    return java.text.SimpleDateFormat("EEE MMM dd yyyy", Locale.US).format(date)
}
