package io.moxd.mocohands_on.model.datasource

import io.moxd.mocohands_on.model.data.RangingReadingDto
import io.moxd.mocohands_on.model.data.RangingStateDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * UWB-Ranging Vertrag für Real & Fake.
 * - state: Idle/Preparing/Ready/Running/Stopped/Error
 * - readings: kontinuierliche Messwerte (Distanz, optional Azimuth/Elevation)
 * - localAddress: (String) für UI-Anzeige "Local address"
 *
 * Signaturen angelehnt an dein Original:
 *   prepareSession(controller: Boolean)
 *   startRanging(remoteAd  r: String): Boolean
 *   stopRanging()
 */
interface UwbRangingProvider {
    val state: StateFlow<RangingStateDto>
    val readings: Flow<RangingReadingDto>
    val localAddress: StateFlow<String>

    fun prepareSession(controller: Boolean)
    fun startRanging(remoteAdr: String): Boolean
    fun stopRanging()
}