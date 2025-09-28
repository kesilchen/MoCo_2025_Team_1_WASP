package io.moxd.mocohands_on.model.modifier

import io.moxd.mocohands_on.model.data.RangingReadingDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExampleRangingModifier(
    private val factor: Double
) : RangingModifier {

    init {
        require(!factor.isNaN()) { "factor must be a valid number" }
    }

    override fun apply(readings: Flow<RangingReadingDto>): Flow<RangingReadingDto> =
        readings.map { reading ->
            RangingReadingDto(
                address = reading.address,
                distanceMeters = reading.distanceMeters?.let { it * factor },
                azimuthDegrees = reading.azimuthDegrees?.let { it * factor },
                elevationDegrees = reading.elevationDegrees?.let { it * factor },
                measurementTimeMillis = reading.measurementTimeMillis
            )
        }
}
