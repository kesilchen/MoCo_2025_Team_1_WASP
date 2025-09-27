package io.moxd.mocohands_on.model.ranging.uwb

import android.content.Context
import androidx.core.uwb.UwbAddress
import androidx.core.uwb.UwbManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.merge

class UnicastUwbProvider(ctx: Context) : UwbProvider {
    private val uwbManager = UwbManager.createInstance(ctx)
    private val uwbControllers = MutableStateFlow<List<UwbController>>(listOf())

    @OptIn(ExperimentalCoroutinesApi::class)
    override val readings = uwbControllers.flatMapLatest { controllers -> merge(*controllers.map { it.readings }.toTypedArray()) }

    override suspend fun prepareSession(nRemotes: Int): List<UwbAddress> {
        val addresses = mutableListOf<UwbAddress>()
        repeat(nRemotes) {
            val controller = UwbController(
                uwbManager = uwbManager,
                rangingMode = UwbRangingMode.Unicast,
            )
            uwbControllers.value += controller
            val address = controller.prepareSession(true)
            addresses.add(address)
        }
        return addresses
    }

    override fun startRanging(
        uwbDevices: List<UwbDeviceConfiguration>,
    ) {
        uwbControllers.value.forEachIndexed { i, controller ->
            controller.startRanging(
                listOf(uwbDevices[i].address),
                uwbDevices[i].sessionId,
                uwbDevices[i].sessionKey
            )
        }
    }
}