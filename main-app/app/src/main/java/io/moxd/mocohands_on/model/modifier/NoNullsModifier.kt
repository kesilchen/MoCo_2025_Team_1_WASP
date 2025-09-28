package io.moxd.mocohands_on.model.modifier

import io.moxd.mocohands_on.model.data.RangingReadingDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class NoNullsModifier(
    private val seedDistanceMeters: Double = 0.0,
    private val seedAzimuthDegrees: Double = 0.0,
    private val seedElevationDegrees: Double = 0.0
) : RangingModifier {

    override fun apply(readings: Flow<RangingReadingDto>): Flow<RangingReadingDto> = flow {
        var lastDistance = seedDistanceMeters
        var lastAzimuth = seedAzimuthDegrees
        var lastElevation = seedElevationDegrees

        readings.collect { r ->
            r.distanceMeters?.let { lastDistance = it }
            r.azimuthDegrees?.let { lastAzimuth = it }
            r.elevationDegrees?.let { lastElevation = it }

            emit(
                RangingReadingDto(
                    address = r.address,
                    distanceMeters = lastDistance,
                    azimuthDegrees = lastAzimuth,
                    elevationDegrees = lastElevation,
                    measurementTimeMillis = r.measurementTimeMillis
                )
            )
        }
    }
}
