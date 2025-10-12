package io.moxd.mocohands_on.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.uwb.UwbAddress
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.moxd.mocohands_on.model.modifier.NoNullsModifier
import io.moxd.mocohands_on.BuildConfig
import io.moxd.mocohands_on.model.database.AppDatabase
import io.moxd.mocohands_on.model.database.stores.DeviceStore
import io.moxd.mocohands_on.model.modifier.MovingAverageModifier
import io.moxd.mocohands_on.model.modifier.OutlierRejectionModifier
import io.moxd.mocohands_on.model.modifier.RangingModifier
import io.moxd.mocohands_on.model.ranging.DefaultRangingProvider
import io.moxd.mocohands_on.model.ranging.oob.FakeOutOfBandProvider
import io.moxd.mocohands_on.model.ranging.oob.ManualOutOfBandProvider
import io.moxd.mocohands_on.model.ranging.uwb.UwbDeviceConfiguration
import io.moxd.mocohands_on.model.ranging.uwb.provider.FakeUwbProvider
import io.moxd.mocohands_on.model.ranging.uwb.provider.UnicastUwbProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class RangingViewModel(app: Application, useFakeData: Boolean, showDebugScreen: Boolean) :
    AndroidViewModel(app) {
    private val db = AppDatabase.getInstance(app.applicationContext)
    private val deviceStore = DeviceStore(db.deviceDao())

    private val manualOutOfBandProvider = ManualOutOfBandProvider(deviceStore.getDeviceCount())
    private val outOfBandProvider =
        if (useFakeData) FakeOutOfBandProvider() else manualOutOfBandProvider

    private val uwbProvider =
        if (useFakeData) FakeUwbProvider(app.applicationContext) else UnicastUwbProvider(app.applicationContext)

    private val rangingProvider = DefaultRangingProvider(outOfBandProvider, uwbProvider)

    private val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    val modifiers = listOf<RangingModifier>(
        OutlierRejectionModifier(),
        NoNullsModifier(),
        MovingAverageModifier()
    )

    val readings = modifiers.fold(rangingProvider.readings) { acc, modifier ->
        modifier.apply(acc)
    }.shareIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        replay = 1
    )

    val devices = rangingProvider.devices
    val state = rangingProvider.state
    val localUwbAddresses = manualOutOfBandProvider.localUwbAddresses

    init {
        Log.d("RangingViewModel", "init")
        viewModelScope.launch(Dispatchers.IO) {
            readings.collect { r ->
                Log.d(
                    "collect",
                    "${dateFormat.format(Date(r.measurementTimeMillis))} " +
                            "${r.address}: ${r.distanceMeters ?: "null"} " +
                            "${r.azimuthDegrees ?: "null"} ${r.elevationDegrees ?: "null"}"
                )
            }
        }
    }

    private var _remoteAddresses = mutableStateListOf("00:00", "00:00")
    val remoteAddresses: List<String> get() = _remoteAddresses

    fun updateRemoteAddress(index: Int, address: String) {
        _remoteAddresses[index] = address
    }

    fun setNumberOfDevices(n: Int) {
//        manualOutOfBandProvider.setNumberOfDevices(n)
        _remoteAddresses = SnapshotStateList(n) { "00:00" }
        restart()
    }

    fun confirm() {
        manualOutOfBandProvider.userInputCallback?.callback(
            remoteAddresses.mapIndexed { index, remoteAddress ->
                UwbDeviceConfiguration(
                    UwbAddress(remoteAddress),
                    sessionId = 42 + index,
                    sessionKey = byteArrayOf(0x08, 0x07, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06)
                )
            }
        )
    }

    fun start() {
        viewModelScope.launch(Dispatchers.IO) {
            rangingProvider.start()
        }
    }

    fun restart() {
        viewModelScope.launch(Dispatchers.IO) {
            rangingProvider.stop()
            rangingProvider.start()
        }
    }

    companion object {
        fun factory(application: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                RangingViewModel(
                    application,
                    useFakeData = BuildConfig.USE_FAKE_DATA,
                    showDebugScreen = BuildConfig.SHOW_DEBUG_SCREEN
                ) as T
        }
    }
}