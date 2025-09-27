package io.moxd.mocohands_on.model.datasource

import io.moxd.mocohands_on.model.data.RangingReadingDto
import io.moxd.mocohands_on.model.data.RangingStateDto
import io.moxd.mocohands_on.model.modifier.RangingModifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface UwbRangingProvider {
    val state: StateFlow<RangingStateDto>
    val localAddress: StateFlow<String>

    fun readings(modifiers: List<RangingModifier> = emptyList()): Flow<RangingReadingDto>
    fun prepareSession(controller: Boolean)
    fun startRanging(remoteAdr: String): Boolean
    fun stopRanging()
}