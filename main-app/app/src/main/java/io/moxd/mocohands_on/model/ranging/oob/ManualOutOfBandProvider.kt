package io.moxd.mocohands_on.model.ranging.oob

import androidx.core.uwb.UwbAddress
import io.moxd.mocohands_on.model.ranging.uwb.UwbDeviceConfiguration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ManualOutOfBandProvider: OutOfBandProvider {
    private val _localUwbAddresses = MutableStateFlow<List<UwbAddress>>(listOf())
    val localUwbAddresses = _localUwbAddresses.asStateFlow()

    var userInputCallback: ManualOobCallback? = null

    private var numberOfDevices = 2

    override suspend fun discoverDevices() = List(numberOfDevices) {}

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun exchangeParameters(localUwbAddresses: List<UwbAddress>) =
        suspendCancellableCoroutine { cont ->
            _localUwbAddresses.value = localUwbAddresses
            userInputCallback = object: ManualOobCallback {
                override fun callback(uwbDeviceConfigurations: List<UwbDeviceConfiguration>) {
                    cont.resume(uwbDeviceConfigurations)
                    userInputCallback = null
                }
            }
        }

    fun setNumberOfDevices(n: Int) {
        numberOfDevices = n
    }
}