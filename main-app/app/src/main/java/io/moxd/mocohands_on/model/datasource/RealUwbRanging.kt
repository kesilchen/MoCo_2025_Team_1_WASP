package io.moxd.mocohands_on.model.datasource

import android.util.Log
import androidx.core.uwb.RangingParameters
import androidx.core.uwb.RangingResult
import androidx.core.uwb.UwbAddress
import androidx.core.uwb.UwbClientSessionScope
import androidx.core.uwb.UwbComplexChannel
import androidx.core.uwb.UwbDevice
import androidx.core.uwb.UwbManager
import io.moxd.mocohands_on.model.data.RangingReadingDto
import io.moxd.mocohands_on.model.data.RangingStateDto
import io.moxd.mocohands_on.model.modifier.RangingModifier
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

class RealUwbRanging(
    private val uwbManager: UwbManager
) : UwbRangingProvider {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var clientSession: UwbClientSessionScope? = null
    private var rangingJob: Job? = null

    private val _state = MutableStateFlow<RangingStateDto>(RangingStateDto.Idle)
    override val state: StateFlow<RangingStateDto> = _state

    private val _readings = MutableSharedFlow<RangingReadingDto>(
        replay = 0, extraBufferCapacity = 64, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override fun readings(modifiers: List<RangingModifier>): Flow<RangingReadingDto> =
        modifiers.fold(_readings as Flow<RangingReadingDto>) { acc, modifier ->
            modifier.apply(acc)
        }

    private val _localAddress = MutableStateFlow("XX:XX")
    override val localAddress: StateFlow<String> = _localAddress

    override fun prepareSession(controller: Boolean) {
        _state.value = RangingStateDto.Preparing
        scope.launch {
            clientSession = if (controller)
                uwbManager.controllerSessionScope()
            else
                uwbManager.controleeSessionScope()

            _localAddress.value = clientSession?.localAddress.toString()

            _state.value = RangingStateDto.Ready(localAddress = _localAddress.value)
        }
    }

    override fun startRanging(remoteAdr: String): Boolean {
        if (clientSession == null) return false

        val remoteUwbAdr = UwbAddress(remoteAdr)
        val partnerParameters = RangingParameters(
            uwbConfigType = RangingParameters.CONFIG_UNICAST_DS_TWR,
            sessionKeyInfo = byteArrayOf(0x08, 0x07, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06),
            complexChannel = UwbComplexChannel(9, 9),
            peerDevices = listOf(UwbDevice(remoteUwbAdr)),
            updateRateType = RangingParameters.RANGING_UPDATE_RATE_FREQUENT,
            sessionId = 42,
            subSessionId = 0,
            subSessionKeyInfo = null
        )

        _state.value = RangingStateDto.Running
        rangingJob?.cancel()
        rangingJob = scope.launch {
            val sessionFlow = clientSession?.prepareSession(partnerParameters)

            sessionFlow?.collect { result ->
                when (result) {
                    is RangingResult.RangingResultPosition -> {
                        Log.d("collect", "${result.position.distance?.value} m")

                        val dist = result.position.distance?.value?.toDouble()
                        if (dist != null) {
                            val az  = result.position.azimuth?.value?.toDouble()
                            val el  = result.position.elevation?.value?.toDouble()
                            _readings.emit(
                                RangingReadingDto(
                                    distanceMeters = dist,
                                    azimuthDeg = az,
                                    elevationDeg = el,
                                    rssi = null
                                )
                            )
                        }
                    }
                    is RangingResult.RangingResultPeerDisconnected -> {
                        Log.d("collect", "Peer disconnected")
                        stopRanging()
                    }
                }
            }
        }
        return true
    }

    override fun stopRanging() {
        rangingJob?.cancel()
        rangingJob = null
        clientSession = null
        _state.value = RangingStateDto.Stopped
    }
}