package io.moxd.mocohands_on.model.ranging.uwb.controller

import android.util.Log
import androidx.core.uwb.RangingParameters
import androidx.core.uwb.RangingResult
import androidx.core.uwb.UwbAddress
import androidx.core.uwb.UwbClientSessionScope
import androidx.core.uwb.UwbComplexChannel
import androidx.core.uwb.UwbDevice
import androidx.core.uwb.UwbManager
import io.moxd.mocohands_on.model.data.RangingReadingDto
import io.moxd.mocohands_on.model.ranging.uwb.UwbRangingMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class UwbController(
    private val uwbManager: UwbManager,
    private val rangingMode: UwbRangingMode,
    private val channel: UwbComplexChannel = UwbComplexChannel(9, 9)
) {
    private val scope = CoroutineScope(Dispatchers.Main.immediate)
    private var clientSession: UwbClientSessionScope? = null
    private var rangingJob: Job? = null

    private val _readings = MutableSharedFlow<RangingReadingDto>(
        replay = 0, extraBufferCapacity = 64, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val readings = _readings.asSharedFlow()

    suspend fun prepareSession(isController: Boolean): UwbAddress {
        Log.d("UwbController", "Preparing session")
        val session = if (isController)
            uwbManager.controllerSessionScope()
        else
            uwbManager.controleeSessionScope()

        clientSession = session

        return session.localAddress
    }

    fun startRanging(
        remoteAddresses: List<UwbAddress>,
        sessionId: Int,
        sessionKey: ByteArray
    ): Boolean {
        if (clientSession == null) return false

        val partnerParameters = RangingParameters(
            uwbConfigType = rangingMode.uwbConfig,
            complexChannel = channel,
            sessionId = sessionId,
            sessionKeyInfo = sessionKey,
            peerDevices = remoteAddresses.map { UwbDevice(it) },
            updateRateType = RangingParameters.RANGING_UPDATE_RATE_FREQUENT,
            subSessionId = 0,
            subSessionKeyInfo = null
        )

        val sessionFlow = clientSession?.prepareSession(partnerParameters)
        if (sessionFlow == null) return false

        rangingJob = scope.launch {
            sessionFlow.collect { result ->
                when (result) {
                    is RangingResult.RangingResultInitialized -> {
                        Log.d("UwbController", "Ranging initialized for ${result.device.address}")
                    }

                    is RangingResult.RangingResultPosition -> {
                        val currentDistance = result.position.distance?.value?.toDouble()
                        val currentAzimuth = result.position.azimuth?.value?.toDouble()
                        val currentElevation = result.position.elevation?.value?.toDouble()
                        Log.d(
                            "UwbController",
                            "Received position ${currentDistance}m ${currentAzimuth}deg ${currentElevation}deg"
                        )
                        _readings.emit(
                            RangingReadingDto(
                                address = result.device.address,
                                distanceMeters = currentDistance,
                                azimuthDegrees = currentAzimuth,
                                elevationDegrees = currentElevation,
                            )
                        )
                    }

                    is RangingResult.RangingResultPeerDisconnected -> {
                        Log.d("UwbController", "Peer disconnected")
                        stopRanging()
                    }
                }
            }
        }
        return true
    }

    fun stopRanging() {
        Log.d("UwbController", "Stopping ranging")
        rangingJob?.cancel()
        rangingJob = null
        clientSession = null
    }
}