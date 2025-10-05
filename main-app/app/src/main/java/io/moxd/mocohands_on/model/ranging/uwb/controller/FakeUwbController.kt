package io.moxd.mocohands_on.model.ranging.uwb.controller

import android.content.Context
import androidx.core.uwb.UwbAddress
import io.moxd.mocohands_on.model.data.RangingReadingDto
import io.moxd.mocohands_on.model.ranging.uwb.logparser.UwbLogParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.BufferOverflow
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

class FakeUwbController(
    private val context: Context,
    private val offset: Double,
    private val instantReplay: Boolean = false,
    private val boardFilter: String = "01:01",
) {
    private var rangingJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val uwbAddress = UwbAddress(Random.nextBytes(2))
    private val assetFileName = "logs.txt"

    private val _readings = MutableSharedFlow<RangingReadingDto>(
        replay = 1,
        extraBufferCapacity = 100_000,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val readings = _readings.asSharedFlow()

    fun startRanging() {
        rangingJob?.cancel()
        rangingJob = scope.launch {
            _readings.subscriptionCount.filter { it > 0 }.first()
            val samples = context.assets.open(assetFileName).use { input ->
                UwbLogParser.parseSamplesWithTime(input, boardFilter)
            }
            if (samples.isEmpty()) return@launch
            val startIndex = computeStartIndex(samples.size, offset)
            if (instantReplay) replayInstant(samples, startIndex) else replayTimed(samples, startIndex)
        }
    }

    private suspend fun replayTimed(samples: List<UwbLogParser.TimedSample>, startIndex: Int) {
        val firstSample = samples[startIndex]
        val logStart = firstSample.timestampMillis
        val wallStart = System.currentTimeMillis()

        var index = startIndex
        while (currentCoroutineContext().isActive && index < samples.size) {
            val sample = samples[index]
            val targetWall = wallStart + (sample.timestampMillis - logStart)
            val waitMs = targetWall - System.currentTimeMillis()
            if (waitMs > 0) delay(waitMs.milliseconds)

            _readings.emit(
                RangingReadingDto(
                    address = uwbAddress,
                    distanceMeters = sample.distanceMeters,
                    azimuthDegrees = sample.azimuthDegrees,
                    elevationDegrees = sample.elevationDegrees,
                    measurementTimeMillis = sample.timestampMillis
                )
            )
            index++
        }
    }

    private suspend fun replayInstant(samples: List<UwbLogParser.TimedSample>, startIndex: Int) {
        var index = startIndex
        while (currentCoroutineContext().isActive && index < samples.size) {
            val sample = samples[index]
            _readings.tryEmit(
                RangingReadingDto(
                    address = uwbAddress,
                    distanceMeters = sample.distanceMeters,
                    azimuthDegrees = sample.azimuthDegrees,
                    elevationDegrees = sample.elevationDegrees,
                    measurementTimeMillis = sample.timestampMillis
                )
            )
            index++
        }
    }

    fun stopRanging() {
        rangingJob?.cancel()
        rangingJob = null
    }

    private fun computeStartIndex(totalSamples: Int, offsetRadians: Double): Int {
        val fraction = (offsetRadians / (2 * PI)) % 1.0
        val rawIndex = (fraction * totalSamples).roundToInt()
        return ((rawIndex % totalSamples) + totalSamples) % totalSamples
    }
}
