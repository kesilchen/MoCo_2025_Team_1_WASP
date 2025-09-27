package io.moxd.mocohands_on.model.ranging

import io.moxd.mocohands_on.model.data.RangingReadingDto
import kotlinx.coroutines.flow.Flow

interface RangingProvider {
    val readings: Flow<RangingReadingDto>
    suspend fun start()
}