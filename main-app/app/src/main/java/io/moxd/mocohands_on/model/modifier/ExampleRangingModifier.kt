package io.moxd.mocohands_on.model.modifier

import io.moxd.mocohands_on.model.data.RangingReadingDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

class ExampleRangingModifier(
    private val factor: Double
) : RangingModifier {

    init {
        require(!factor.isNaN()) { "factor must be a valid number" }
    }

    override fun apply(readings: Flow<RangingReadingDto>): Flow<RangingReadingDto> =
        readings.map { reading ->
            RangingReadingDto(
                distanceMeters = reading.distanceMeters * factor,
                azimuthDeg = reading.azimuthDeg?.let { it * factor },
                elevationDeg = reading.elevationDeg?.let { it * factor },
                rssi = reading.rssi?.let { (it * factor).roundToInt() },
                timestampMillis = reading.timestampMillis
            )
        }
}
