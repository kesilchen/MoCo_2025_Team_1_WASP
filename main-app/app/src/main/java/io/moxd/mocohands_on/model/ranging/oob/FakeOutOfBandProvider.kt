package io.moxd.mocohands_on.model.ranging.oob

import androidx.core.uwb.UwbAddress
import io.moxd.mocohands_on.model.ranging.uwb.UwbDeviceConfiguration

class FakeOutOfBandProvider : OutOfBandProvider {
    override suspend fun discoverDevices() = List(2) {}

    override suspend fun exchangeParameters(localUwbAddresses: List<UwbAddress>): List<UwbDeviceConfiguration> =
        listOf()
}