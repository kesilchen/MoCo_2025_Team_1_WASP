package de.thkoeln.uwbee.connectivity.uwb

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.uwb.RangingParameters
import androidx.core.uwb.RangingResult
import androidx.core.uwb.UwbAddress
import androidx.core.uwb.UwbComplexChannel
import androidx.core.uwb.UwbControllerSessionScope
import androidx.core.uwb.UwbDevice
import androidx.core.uwb.UwbManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class UwbController(ctx: Context) {
    var job: Job? = null
    val uwbManager = UwbManager.createInstance(ctx)
    var clientSession by mutableStateOf<UwbControllerSessionScope?>(null)

    suspend fun prepare() {
        clientSession = uwbManager.controllerSessionScope()
        val localAddr = clientSession?.localAddress.toString()
        Log.d("uwb ranging", localAddr)
    }

    fun startRanging() {
        val partnerAddress : Pair<UwbAddress, UwbComplexChannel> = Pair(UwbAddress("00:00"),
            UwbComplexChannel(9, 9))

        val partnerParameters = RangingParameters(
            uwbConfigType = RangingParameters.CONFIG_MULTICAST_DS_TWR,
            sessionKeyInfo = byteArrayOf(0x08, 0x07, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06),
            complexChannel = partnerAddress.second,
            peerDevices = listOf(UwbDevice(partnerAddress.first), UwbDevice(UwbAddress("00:01"))),
            updateRateType = RangingParameters.RANGING_UPDATE_RATE_AUTOMATIC,
            sessionId = 42,
            subSessionId = 0,
            subSessionKeyInfo = null,
        )

//        val clientSession = uwbManager.controllerSessionScope()
//        val localAddr = clientSession.localAddress.toString()
//        Log.d("uwb ranging", localAddr)

        val sessionFlow = clientSession?.prepareSession(partnerParameters)

        CoroutineScope(Dispatchers.Main.immediate).launch {
            Log.d("uwb ranging", "STARTING RANGING")
            sessionFlow?.collect {
                when(it) {
                    is RangingResult.RangingResultPosition -> Log.d("uwb ranging", it.position.distance?.value.toString())
                    is RangingResult.RangingResultPeerDisconnected -> cancelRanging()
                }
            }
        }
    }

    fun cancelRanging() {
        job?.cancel()
        Log.d("uwb ranging", "RANGING ENDED")
    }
}