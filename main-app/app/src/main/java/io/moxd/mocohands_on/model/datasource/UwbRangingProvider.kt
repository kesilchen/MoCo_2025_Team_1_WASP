package io.moxd.mocohands_on.model.datasource

import io.moxd.mocohands_on.model.data.RangingReadingDto
import io.moxd.mocohands_on.model.data.RangingStateDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface UwbRangingProvider {
    val state: StateFlow<RangingStateDto>
    val readings: Flow<RangingReadingDto>
    val localAddress: StateFlow<String>

    fun prepareSession(controller: Boolean)
    fun startRanging(remoteAdr: String): Boolean
    fun stopRanging()
}