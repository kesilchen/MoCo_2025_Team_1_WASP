package io.moxd.mocohands_on.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.uwb.UwbAddress
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.moxd.mocohands_on.BuildConfig
import io.moxd.mocohands_on.model.ranging.RealRangingProvider
import io.moxd.mocohands_on.model.ranging.oob.FakeOutOfBandProvider
import io.moxd.mocohands_on.model.ranging.oob.ManualOutOfBandProvider
import io.moxd.mocohands_on.model.ranging.uwb.FakeUwbProvider
import io.moxd.mocohands_on.model.ranging.uwb.UnicastUwbProvider
import io.moxd.mocohands_on.model.ranging.uwb.UwbDeviceConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class NewRangingViewModel(app: Application, useFakeData: Boolean, showDebugScreen: Boolean) :
    AndroidViewModel(app) {
    private val manualOutOfBandProvider = ManualOutOfBandProvider()
    private val outOfBandProvider =
        if (showDebugScreen) manualOutOfBandProvider else FakeOutOfBandProvider()

    private val uwbProvider =
        if (useFakeData) FakeUwbProvider() else UnicastUwbProvider(app.applicationContext)

    private val rangingProvider = RealRangingProvider(outOfBandProvider, uwbProvider)

    init {
        Log.d("RangingViewModel", "init")
        viewModelScope.launch(Dispatchers.IO) {
            rangingProvider.start()
        }
    }

    val remoteAddress = mutableStateOf("00:00")

    val readings = rangingProvider.readings.shareIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        replay = 1
    )
    val state = rangingProvider.state

    val localUwbAddresses = manualOutOfBandProvider.localUwbAddresses

    fun confirm() {
        manualOutOfBandProvider.userInputCallback?.callback(
            listOf(
                UwbDeviceConfiguration(
                    UwbAddress(remoteAddress.value),
                    sessionId = 42,
                    sessionKey = byteArrayOf(0x08, 0x07, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06)
                )
            )
        )
    }

    fun restart() {
        viewModelScope.launch(Dispatchers.IO) {
            rangingProvider.stop()
            rangingProvider.start()
        }
    }

    fun stop() {
        viewModelScope.launch(Dispatchers.IO) {
            rangingProvider.stop()
        }
    }

    companion object {
        fun factory(application: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                NewRangingViewModel(
                    application,
                    useFakeData = BuildConfig.USE_FAKE_DATA,
                    showDebugScreen = BuildConfig.SHOW_DEBUG_SCREEN
                ) as T
        }
    }
}