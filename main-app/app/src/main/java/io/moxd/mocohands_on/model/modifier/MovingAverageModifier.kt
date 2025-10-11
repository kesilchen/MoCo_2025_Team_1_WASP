package io.moxd.mocohands_on.model.modifier

import io.moxd.mocohands_on.model.data.RangingReadingDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class MovingAverageModifier(
    private val windowSize: Int = 20
) : RangingModifier {

    override fun apply(readings: Flow<RangingReadingDto>): Flow<RangingReadingDto> = flow {
        val azimuthBuffers = mutableMapOf<Any, ArrayDeque<Double>>()
        val elevationBuffers = mutableMapOf<Any, ArrayDeque<Double>>()

        readings.collect { reading ->
            val key = reading.address
            val azimuthBuffer = azimuthBuffers.getOrPut(key) { ArrayDeque() }
            val elevationBuffer = elevationBuffers.getOrPut(key) { ArrayDeque() }

            reading.azimuthDegrees?.let {
                azimuthBuffer.addLast(it)
                if (azimuthBuffer.size > windowSize) azimuthBuffer.removeFirst()
            }
            reading.elevationDegrees?.let {
                elevationBuffer.addLast(it)
                if (elevationBuffer.size > windowSize) elevationBuffer.removeFirst()
            }

            if (azimuthBuffer.size == windowSize && elevationBuffer.size == windowSize) {
                val smoothedAzimuth = circularMeanDegrees(azimuthBuffer)
                val smoothedElevation = elevationBuffer.average()
                emit(reading.copy(azimuthDegrees = smoothedAzimuth, elevationDegrees = smoothedElevation))
            }
        }
    }

    private fun circularMeanDegrees(values: Collection<Double>): Double {
        var x = 0.0
        var y = 0.0
        for (degrees in values) {
            val radians = Math.toRadians(degrees)
            x += cos(radians)
            y += sin(radians)
        }
        return Math.toDegrees(atan2(y, x))
    }
}