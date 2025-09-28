package io.moxd.mocohands_on.model.ranging

import io.moxd.mocohands_on.model.data.RangingStateDto
import io.moxd.mocohands_on.model.ranging.oob.OutOfBandProvider
import io.moxd.mocohands_on.model.ranging.uwb.UwbProvider
import kotlinx.coroutines.flow.MutableStateFlow

class RealRangingProvider(
    private val oobProvider: OutOfBandProvider,
    private val uwbProvider: UwbProvider
) : RangingProvider {
    override val readings = uwbProvider.readings
    override val state = MutableStateFlow<RangingStateDto>(RangingStateDto.Idle)

    override suspend fun start() {
        state.value = RangingStateDto.Preparing

        val uwbDevices = oobProvider.discoverDevices()
        val localUwbAddresses = uwbProvider.prepareSession(uwbDevices)
        val remoteUwbDevices = oobProvider.exchangeParameters(localUwbAddresses)
        state.value = RangingStateDto.Ready("")

        uwbProvider.startRanging(remoteUwbDevices)
        state.value = RangingStateDto.Running
    }
}