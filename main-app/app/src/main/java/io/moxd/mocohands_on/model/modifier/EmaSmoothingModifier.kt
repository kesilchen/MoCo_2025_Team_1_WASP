package io.moxd.mocohands_on.model.modifier

import io.moxd.mocohands_on.model.data.RangingReadingDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Exponential Moving Average (EMA) over the readings.
 *
 * Formula:
 *   EMA_t = alpha * x_t + (1 - alpha) * EMA_{t-1}
 *
 * alpha ∈ (0,1]:
 *   - small values (e.g. 0.2) -> more smoothed, but slower
 *   - large values (e.g. 0.5) -> less smoothed, but faster
 *
 */
class EmaSmoothingModifier(
    private val alpha: Double = 0.35
) : RangingModifier {

    override fun apply(readings: Flow<RangingReadingDto>): Flow<RangingReadingDto> = flow {
        var emaDistance: Double? = null
        var emaAzimuthX: Double? = null
        var emaAzimuthY: Double? = null
        var emaElevationX: Double? = null
        var emaElevationY: Double? = null

        readings.collect { reading ->
            emaDistance = if (emaDistance == null) {
                reading.distanceMeters!!
            } else {
                alpha * reading.distanceMeters!! + (1 - alpha) * emaDistance!!
            }

            val azimuthRadians = Math.toRadians(reading.azimuthDegrees!!)
            val azimuthX = cos(azimuthRadians)
            val azimuthY = sin(azimuthRadians)
            emaAzimuthX = if (emaAzimuthX == null) azimuthX else alpha * azimuthX + (1 - alpha) * emaAzimuthX!!
            emaAzimuthY = if (emaAzimuthY == null) azimuthY else alpha * azimuthY + (1 - alpha) * emaAzimuthY!!
            val averageAzimuthDegrees = Math.toDegrees(atan2(emaAzimuthY, emaAzimuthX))

            val elevationRadians = Math.toRadians(reading.elevationDegrees!!)
            val elevationX = cos(elevationRadians)
            val elevationY = sin(elevationRadians)
            emaElevationX = if (emaElevationX == null) elevationX else alpha * elevationX + (1 - alpha) * emaElevationX!!
            emaElevationY = if (emaElevationY == null) elevationY else alpha * elevationY + (1 - alpha) * emaElevationY!!
            val averageElevationDegrees = Math.toDegrees(atan2(emaElevationY, emaElevationX))

            emit(
                RangingReadingDto(
                    address = reading.address,
                    distanceMeters = emaDistance,
                    azimuthDegrees = averageAzimuthDegrees,
                    elevationDegrees = averageElevationDegrees,
                    measurementTimeMillis = reading.measurementTimeMillis,
                )
            )
        }
    }
}
