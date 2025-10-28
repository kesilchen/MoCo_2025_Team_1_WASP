package io.moxd.mocohands_on.viewmodel

import android.app.Application
import androidx.compose.runtime.collectAsState
import androidx.core.uwb.UwbAddress
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.moxd.mocohands_on.model.database.AppDatabase
import io.moxd.mocohands_on.model.database.entities.Device
import io.moxd.mocohands_on.model.database.entities.DeviceWithPeripheralConnector
import io.moxd.mocohands_on.model.database.entities.PeripheralConnector
import io.moxd.mocohands_on.model.database.stores.DeviceStore
import io.moxd.mocohands_on.model.database.stores.PeripheralConnectorStore
import io.moxd.mocohands_on.model.peripherals.PeripheralConnectorType
import io.moxd.mocohands_on.model.peripherals.uwbeesp32.DeviceInfo
import io.moxd.mocohands_on.model.peripherals.uwbeesp32.LedState
import io.moxd.mocohands_on.model.peripherals.uwbeesp32.UWBeEsp32Service
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.ConnectException

sealed class TestResult {
    data class Success(val deviceInfo: DeviceInfo) : TestResult()
    data class Error(val message: String) : TestResult()
}

class SetupViewModel(app: Application) :
    AndroidViewModel(app) {

    private val db = AppDatabase.getInstance(app.applicationContext)
    private val deviceStore = DeviceStore(db.deviceDao())
    private val peripheralConnectorStore = PeripheralConnectorStore(db.peripheralConnectorDao())

    fun createDevice(
        name: String,
        uwbAddress: String,
        uwbSessionId: Int,
        peripheralType: PeripheralConnectorType,
        peripheralApiUrl: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val peripheralConnectorId = peripheralConnectorStore.insertPeripheralConnector(
                PeripheralConnector(
                    type = peripheralType,
                    apiUrl = peripheralApiUrl
                )
            )

            deviceStore.insertDevice(
                Device(
                    name = name,
                    uwbAddress = uwbAddress,
                    uwbSessionId = uwbSessionId,
                    peripheralConnectorId = peripheralConnectorId
                )
            )
        }
    }

    val devices = deviceStore.listDevicesWithPeripheralConnector()

//    fun createPeripheralConnector(type: PeripheralConnectorType, apiUrl: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            peripheralConnectorStore.insertPeripheralConnector(PeripheralConnector(
//                type = type,
//                apiUrl = apiUrl
//            ))
//        }
//    }

    suspend fun testEsp32Connection(ipAddress: String): TestResult {
        val apiService = UWBeEsp32Service.createApiService("http://$ipAddress/")

        try {
            val deviceInfo = apiService.getDeviceInfo()
            return TestResult.Success(deviceInfo)
        } catch (e: ConnectException) {
            return TestResult.Error(e.message ?: "Unable to connect to the device.")
        }
    }

    fun setEsp32LedStatus(address: UwbAddress, state: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val device = deviceStore.getDeviceWithPeripheralConnectorByAddress(address)

            if (device != null) {
                val apiService =
                    UWBeEsp32Service.createApiService(device.peripheralConnector.apiUrl)

                try {
                    apiService.toggleLed()
                } catch (e: ConnectException) {

                }
            }
        }
    }
}