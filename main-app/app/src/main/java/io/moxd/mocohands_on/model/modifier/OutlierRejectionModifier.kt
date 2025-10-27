package io.moxd.mocohands_on.model.modifier

import io.moxd.mocohands_on.model.data.RangingReadingDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.abs

class OutlierRejectionModifier(
    private val azimuthWindowSize: Int = 5,
    private val azimuthMaxDeviation: Double = 90.0,
    private val elevationWindowSize: Int = 7,
    private val elevationMaxDeviation: Double = 50.0
) : RangingModifier {

    private data class DeviceState(
        val azimuthHistory: ArrayDeque<Double> = ArrayDeque(),
        val elevationHistory: ArrayDeque<Double> = ArrayDeque()
    )

    private val deviceStates = mutableMapOf<Any, DeviceState>()

    override fun apply(readings: Flow<RangingReadingDto>): Flow<RangingReadingDto> = flow {
        readings.collect { reading ->
            val state = deviceStates.getOrPut(reading.address) { DeviceState() }

            var azimuth = reading.azimuthDegrees
            var elevation = reading.elevationDegrees

            if (azimuth != null) {
                val mean = if (state.azimuthHistory.isEmpty()) {
                    azimuth
                } else {
                    circularMeanDegrees(state.azimuthHistory)
                }
                val deviation = abs(minimalAngularDifferenceDegrees(azimuth, mean))
                if (deviation > azimuthMaxDeviation) {
                    azimuth = null
                } else {
                    state.azimuthHistory.addLast(azimuth)
                    if (state.azimuthHistory.size > azimuthWindowSize) state.azimuthHistory.removeFirst()
                }
            }

            if (elevation != null) {
                val mean = if (state.elevationHistory.isEmpty()) {
                    elevation
                } else {
                    circularMeanDegrees(state.elevationHistory)
                }
                val deviation = abs(minimalAngularDifferenceDegrees(elevation, mean))
                if (deviation > elevationMaxDeviation) {
                    elevation = null
                } else {
                    state.elevationHistory.addLast(elevation)
                    if (state.elevationHistory.size > elevationWindowSize) state.elevationHistory.removeFirst()
                }
            }

            emit(reading.copy(azimuthDegrees = azimuth, elevationDegrees = elevation))
        }
    }
}