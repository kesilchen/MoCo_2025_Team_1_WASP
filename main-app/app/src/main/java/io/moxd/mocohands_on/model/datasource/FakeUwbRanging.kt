package io.moxd.mocohands_on.model.datasource

import io.moxd.mocohands_on.model.data.RangingReadingDto
import io.moxd.mocohands_on.model.data.RangingStateDto
import io.moxd.mocohands_on.model.modifier.RangingModifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

class FakeUwbRanging : UwbRangingProvider {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _state = MutableStateFlow<RangingStateDto>(RangingStateDto.Idle)
    override val state: StateFlow<RangingStateDto> = _state

    private val _readings = MutableSharedFlow<RangingReadingDto>(
        replay = 0, extraBufferCapacity = 64, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override fun readings(modifiers: List<RangingModifier>): Flow<RangingReadingDto> =
        modifiers.fold(_readings as Flow<RangingReadingDto>) { acc, modifier ->
            modifier.apply(acc)
        }

    private val _localAddress = MutableStateFlow("FA:KE")
    override val localAddress: StateFlow<String> = _localAddress

    private var job: Job? = null

    override fun prepareSession(controller: Boolean) {
        _state.value = RangingStateDto.Ready(localAddress = _localAddress.value)
    }

    override fun startRanging(remoteAdr: String): Boolean {
        _state.value = RangingStateDto.Running
        job?.cancel()
        job = scope.launch {
            var time = 0.0
            while (isActive) {
                val currentDistance = 1.5 + 0.5 * sin(time)
                val currentAzimuth  = 30.0 * sin(time / 2.0)
                val currentElevation     = 8.0 * sin(time / 3.0)
                _readings.emit(
                    RangingReadingDto(
                        distanceMeters = currentDistance,
                        azimuthDegrees = currentAzimuth,
                        elevationDegrees = currentElevation
                    )
                )
                time += 2 * PI / 60
                delay(50.milliseconds)
            }
        }
        return true
    }

    override fun stopRanging() {
        job?.cancel()
        job = null
        _state.value = RangingStateDto.Stopped
    }
}