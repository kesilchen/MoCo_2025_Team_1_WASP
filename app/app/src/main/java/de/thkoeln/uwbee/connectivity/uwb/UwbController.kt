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

class UwbController(val mac: String, val sessionId: Int, val uwbManager: UwbManager, val sts: ByteArray
//, ctx: Context
) {
    var job: Job? = null
//    val uwbManager = UwbManager.createInstance(ctx)
    var clientSession by mutableStateOf<UwbControllerSessionScope?>(null)

    suspend fun prepare() {
        clientSession = uwbManager.controllerSessionScope()
        val localAddr = clientSession?.localAddress.toString()
        Log.d("uwb ranging", localAddr)
    }

    suspend fun addControlee() {
        clientSession?.addControlee(UwbAddress("02:02"))
    }

    fun startRanging() {
        val partnerAddress : Pair<UwbAddress, UwbComplexChannel> = Pair(UwbAddress(mac),
            UwbComplexChannel(9, 9))

        val partnerParameters = RangingParameters(
            uwbConfigType = RangingParameters.CONFIG_UNICAST_DS_TWR,
            sessionKeyInfo = sts,
            complexChannel = partnerAddress.second,
            peerDevices = listOf(UwbDevice(partnerAddress.first)),
            updateRateType = RangingParameters.RANGING_UPDATE_RATE_FREQUENT,
            sessionId = sessionId,
            subSessionId = 0,
            subSessionKeyInfo = null,
        )

//        val clientSession = uwbManager.controllerSessionScope()
//        val localAddr = clientSession.localAddress.toString()
//        Log.d("uwb ranging", localAddr)

        val sessionFlow = clientSession?.prepareSession(partnerParameters)

        CoroutineScope(Dispatchers.Main.immediate).launch {
            Log.d("uwb ranging $mac", "STARTING RANGING")
            sessionFlow?.collect {
                when(it) {
                    is RangingResult.RangingResultPosition -> Log.d("uwb ranging $mac", it.position.distance?.value.toString())
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