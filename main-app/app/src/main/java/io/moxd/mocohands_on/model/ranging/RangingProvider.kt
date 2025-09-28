package io.moxd.mocohands_on.model.ranging

import io.moxd.mocohands_on.model.data.RangingReadingDto
import io.moxd.mocohands_on.model.data.RangingStateDto
import io.moxd.mocohands_on.model.ranging.uwb.UwbDeviceConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface RangingProvider {
    val readings: Flow<RangingReadingDto>
    val devices: StateFlow<List<Unit>>
    val state: StateFlow<RangingStateDto>
    suspend fun start()
    suspend fun stop()
}