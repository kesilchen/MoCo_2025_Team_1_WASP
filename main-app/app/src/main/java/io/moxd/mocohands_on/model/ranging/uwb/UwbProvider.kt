package io.moxd.mocohands_on.model.ranging.uwb

import androidx.core.uwb.UwbAddress
import io.moxd.mocohands_on.model.data.RangingReadingDto
import kotlinx.coroutines.flow.Flow

interface UwbProvider {
    val readings: Flow<RangingReadingDto>
    suspend fun prepareSession(nRemotes: Int): List<UwbAddress>
    fun startRanging(uwbDevices: List<UwbDeviceConfiguration>)
}