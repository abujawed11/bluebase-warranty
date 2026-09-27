package com.sunrack.bluebase.core.util

/** Ports `mapCodeToCity.ts`. */
fun mapCodeToCity(code: String?): String = when (code) {
    "RH" -> "Ranchi"
    "BS" -> "Boisar"
    "CN" -> "Chennai"
    else -> "Unknown"
}
