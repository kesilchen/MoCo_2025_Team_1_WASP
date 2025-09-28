package io.moxd.mocohands_on.model.ranging.uwb

import androidx.core.uwb.UwbAddress
import io.moxd.mocohands_on.model.data.RangingReadingDto
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
import kotlin.math.sin
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

class FakeUwbController(val offset: Double) {
    private var rangingJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val address = UwbAddress(Random.nextBytes(2))

    private val _readings = MutableSharedFlow<RangingReadingDto>(
        replay = 0, extraBufferCapacity = 64, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val readings = _readings.asSharedFlow()

    fun startRanging() {
        rangingJob?.cancel()
        rangingJob = scope.launch {
            var time = 0.0 + offset
            while (isActive) {
                val currentDistance = 1.5 + 0.5 * sin(time)
                val currentAzimuth = 30.0 * sin(time / 2.0)
                val currentElevation = 8.0 * sin(time / 3.0)
                _readings.emit(
                    RangingReadingDto(
                        address = address,
                        distanceMeters = currentDistance,
                        azimuthDegrees = currentAzimuth,
                        elevationDegrees = currentElevation
                    )
                )
                time += 2 * PI / 60
                delay(50.milliseconds)
            }
        }
    }

    fun stopRanging() {
        rangingJob?.cancel()
        rangingJob = null
    }
}