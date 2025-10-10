package io.moxd.mocohands_on.model.peripherals.uwbeesp32

import java.util.UUID

data class DeviceInfo(
    val deviceId: UUID,
    val deviceType: DeviceType
)