package io.moxd.mocohands_on.util

fun isValidMac(mac: String): Boolean {
    val macRegex = Regex("^[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}$")
    return macRegex.matches(mac)
}

fun isValidSessionId(sessionId: String): Boolean {
    return sessionId.all { it.isDigit() } && !sessionId.isEmpty()
}

fun isValidIpv4(ip: String): Boolean {
    val regex = Regex("^(((?!25?[6-9])[12]\\d|[1-9])?\\d\\.?\\b){4}$")
    return regex.matches(ip)
}
