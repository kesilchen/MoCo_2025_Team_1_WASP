package io.moxd.mocohands_on.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.core.uwb.UwbAddress
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.moxd.mocohands_on.model.ranging.RealRangingProvider
import io.moxd.mocohands_on.model.ranging.oob.ManualOutOfBandProvider
import io.moxd.mocohands_on.model.ranging.uwb.UnicastUwbProvider
import io.moxd.mocohands_on.model.ranging.uwb.UwbDeviceConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class NewRangingViewModel(app: Application) : AndroidViewModel(app) {
    private val outOfBandProvider = ManualOutOfBandProvider()
    private val uwbProvider = UnicastUwbProvider(app.applicationContext)
    private val rangingProvider = RealRangingProvider(outOfBandProvider, uwbProvider)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            rangingProvider.start()
        }
    }

    val readings = rangingProvider.readings.shareIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        replay = 1
    )
    val localUwbAddresses = outOfBandProvider.localUwbAddresses

    val remoteAddress = mutableStateOf("")
    fun confirm() = outOfBandProvider.userInputCallback?.callback(
        listOf(
            UwbDeviceConfiguration(
                UwbAddress(remoteAddress.value),
                sessionId = 42,
                sessionKey = byteArrayOf(0x08, 0x07, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06)
            )
        )
    )
}