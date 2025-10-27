package io.moxd.mocohands_on.model.modifier

import androidx.core.uwb.UwbAddress
import io.moxd.mocohands_on.model.data.RangingReadingDto
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OutlierRejectionModifierTest {

    private val address = UwbAddress.fromBytes(byteArrayOf(0x01, 0x02))

    @Test
    fun wrapAroundAzimuthIsPreserved() = runBlocking {
        val modifier = OutlierRejectionModifier(
            azimuthWindowSize = 3,
            azimuthMaxDeviation = 30.0,
            elevationWindowSize = 3,
            elevationMaxDeviation = 30.0
        )

        val readings = listOf(179.0, -179.0, 178.0).map { azimuth ->
            RangingReadingDto(
                address = address,
                azimuthDegrees = azimuth,
                elevationDegrees = null
            )
        }

        val results = modifier.apply(readings.asFlow()).toList()

        assertEquals(listOf(179.0, -179.0, 178.0), results.map { it.azimuthDegrees })
    }

    @Test
    fun distantAzimuthOutlierIsRejected() = runBlocking {
        val modifier = OutlierRejectionModifier(
            azimuthWindowSize = 4,
            azimuthMaxDeviation = 25.0,
            elevationWindowSize = 4,
            elevationMaxDeviation = 25.0
        )

        val readings = listOf(10.0, 12.0, 11.0, 200.0).map { azimuth ->
            RangingReadingDto(
                address = address,
                azimuthDegrees = azimuth,
                elevationDegrees = null
            )
        }

        val results = modifier.apply(readings.asFlow()).toList()

        assertEquals(listOf(10.0, 12.0, 11.0, null), results.map { it.azimuthDegrees })
        assertNull(results.last().azimuthDegrees)
    }
}
