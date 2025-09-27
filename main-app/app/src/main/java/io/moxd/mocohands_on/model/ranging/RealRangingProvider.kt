package io.moxd.mocohands_on.model.ranging

import io.moxd.mocohands_on.model.ranging.oob.OutOfBandProvider
import io.moxd.mocohands_on.model.ranging.uwb.UwbProvider

class RealRangingProvider(
    private val oobProvider: OutOfBandProvider,
    private val uwbProvider: UwbProvider
) : RangingProvider {
    override val readings = uwbProvider.readings

    override suspend fun start() {
        val uwbDevices = oobProvider.discoverDevices()
        val localUwbAddresses = uwbProvider.prepareSession(uwbDevices)
        val remoteUwbDevices = oobProvider.exchangeParameters(localUwbAddresses)
        uwbProvider.startRanging(remoteUwbDevices)
    }
}