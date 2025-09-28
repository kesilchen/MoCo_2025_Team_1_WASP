package io.moxd.mocohands_on.model.ranging.uwb.controller

import android.content.Context
import androidx.core.uwb.UwbAddress
import io.moxd.mocohands_on.model.data.RangingReadingDto
import io.moxd.mocohands_on.model.ranging.uwb.logparser.UwbLogParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

class FakeUwbController(
    private val context: Context,
    val offset: Double
) {
    private var rangingJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val address = UwbAddress(Random.nextBytes(2))

    private val _readings = MutableSharedFlow<RangingReadingDto>(
        replay = 0, extraBufferCapacity = 64, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val readings = _readings.asSharedFlow()

    private val assetFileName: String = "logs.txt"

    fun startRanging() {
        rangingJob?.cancel()
        rangingJob = scope.launch {
            val input = context.assets.open(assetFileName)
            val triples = UwbLogParser.parseTriplesFromLog(input, board = "01:01")
            input.close()

            if (triples.isEmpty()) return@launch

            val startIndex = run {
                val offsetFraction = (offset / (2 * PI)) % 1.0
                val rawIndex = (offsetFraction * triples.size).roundToInt()
                val numberOfDataPoints = triples.size
                ((rawIndex % numberOfDataPoints) + numberOfDataPoints) % numberOfDataPoints
            }

            var index = startIndex
            while (isActive) {
                val (currentDistance, currentAzimuth, currentElevation) = triples[index]

                _readings.emit(
                    RangingReadingDto(
                        address = address,
                        distanceMeters = currentDistance,
                        azimuthDegrees = currentAzimuth,
                        elevationDegrees = currentElevation
                    )
                )

                index++
                if (index >= triples.size) index = 0

                delay(50.milliseconds)
            }
        }
    }

    fun stopRanging() {
        rangingJob?.cancel()
        rangingJob = null
    }
}
