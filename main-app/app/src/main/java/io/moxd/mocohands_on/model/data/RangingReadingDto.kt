package io.moxd.mocohands_on.model.data

data class RangingReadingDto(
    val distanceMeters: Double? = null,
    val azimuthDegrees: Double? = null,
    val elevationDegrees: Double? = null,
    val measurementTimeMillis: Long = System.currentTimeMillis()
)