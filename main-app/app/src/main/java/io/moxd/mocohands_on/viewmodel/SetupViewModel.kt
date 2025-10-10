package io.moxd.mocohands_on.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay

sealed class TestResult {
    object Success : TestResult()
    data class Error(val message: String) : TestResult()
}

class SetupViewModel() :
    ViewModel() {

    suspend fun testEsp32Connection(ipAddress: String): TestResult {
        delay(1500)
        return if (ipAddress.startsWith("192.168.")) {
            TestResult.Success
        } else {
            TestResult.Error("Could not reach device.")
        }
    }
}