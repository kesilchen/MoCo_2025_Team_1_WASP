package io.moxd.mocohands_on.model.ranging

import io.moxd.mocohands_on.model.data.RangingStateDto
import io.moxd.mocohands_on.model.ranging.oob.OutOfBandProvider
import io.moxd.mocohands_on.model.ranging.uwb.UwbProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RealRangingProvider(
    private val oobProvider: OutOfBandProvider,
    private val uwbProvider: UwbProvider
) : RangingProvider {
    override val readings = uwbProvider.readings
    private val _devices = MutableStateFlow<List<Unit>>(listOf())
    override val devices = _devices.asStateFlow()
    override val state = MutableStateFlow<RangingStateDto>(RangingStateDto.Idle)

    override suspend fun start() {
        state.value = RangingStateDto.Preparing

        val uwbDevices = oobProvider.discoverDevices()
        _devices.value = uwbDevices
        val localUwbAddresses = uwbProvider.prepareSession(uwbDevices.size)
        val remoteUwbDevices = oobProvider.exchangeParameters(localUwbAddresses)
        state.value = RangingStateDto.Ready("")

        uwbProvider.startRanging(remoteUwbDevices)
        state.value = RangingStateDto.Running
    }

    override suspend fun stop() {
        uwbProvider.stopRanging()
        state.value = RangingStateDto.Stopped
    }
}