package io.moxd.mocohands_on.model.data

data class RangingReadingDto(
    val distanceMeters: Double,
    val azimuthDegrees: Double? = null,
    val elevationDegrees: Double? = null,
    val measurementTimeMillis: Long = System.currentTimeMillis()
)