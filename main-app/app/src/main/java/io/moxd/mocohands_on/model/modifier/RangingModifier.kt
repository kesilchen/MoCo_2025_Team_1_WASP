package io.moxd.mocohands_on.model.modifier

import io.moxd.mocohands_on.model.data.RangingReadingDto
import kotlinx.coroutines.flow.Flow

/**
 * Represents a composable transformation that can enrich, filter, or otherwise mutate
 * the stream of ranging readings before it is consumed by the UI.
 */
fun interface RangingModifier {
    fun apply(readings: Flow<RangingReadingDto>): Flow<RangingReadingDto>
}