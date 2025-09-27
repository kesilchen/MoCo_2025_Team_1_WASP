package io.moxd.mocohands_on.model.ranging.oob

import androidx.core.uwb.UwbAddress
import io.moxd.mocohands_on.model.ranging.uwb.UwbDeviceConfiguration

interface OutOfBandProvider {
    suspend fun discoverDevices(): Int
    suspend fun exchangeParameters(localUwbAddresses: List<UwbAddress>): List<UwbDeviceConfiguration>
}