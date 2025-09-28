package io.moxd.mocohands_on.model.data

import androidx.core.uwb.UwbAddress

data class RangingReadingDto(
    val address: UwbAddress,
    val distanceMeters: Double? = null,
    val azimuthDegrees: Double? = null,
    val elevationDegrees: Double? = null,
    val measurementTimeMillis: Long = System.currentTimeMillis()
)