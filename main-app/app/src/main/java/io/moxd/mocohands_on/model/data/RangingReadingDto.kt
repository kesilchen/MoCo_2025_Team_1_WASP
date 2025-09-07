package io.moxd.mocohands_on.model.data

data class RangingReadingDto(
    val distanceMeters: Double,
    val azimuthDeg: Double? = null,
    val elevationDeg: Double? = null,
    val rssi: Int? = null,
    val timestampMillis: Long = System.currentTimeMillis()
)