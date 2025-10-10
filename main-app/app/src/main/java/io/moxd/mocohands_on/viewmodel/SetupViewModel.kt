package io.moxd.mocohands_on.viewmodel

import androidx.lifecycle.ViewModel
import io.moxd.mocohands_on.model.peripherals.uwbeesp32.DeviceInfo
import io.moxd.mocohands_on.model.peripherals.uwbeesp32.UWBeEsp32Service
import java.net.ConnectException

sealed class TestResult {
    data class Success(val deviceInfo: DeviceInfo) : TestResult()
    data class Error(val message: String) : TestResult()
}

class SetupViewModel() :
    ViewModel() {

    suspend fun testEsp32Connection(ipAddress: String): TestResult {
        val apiService = UWBeEsp32Service.createApiService(ipAddress)

        try {
            val deviceInfo = apiService.getDeviceInfo()
            return TestResult.Success(deviceInfo)
        } catch (e: ConnectException) {
            return TestResult.Error(e.message ?: "Unable to connect to the device.")
        }
    }
}