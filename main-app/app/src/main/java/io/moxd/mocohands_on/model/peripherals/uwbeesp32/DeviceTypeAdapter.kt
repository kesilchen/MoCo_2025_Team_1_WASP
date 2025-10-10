package io.moxd.mocohands_on.model.peripherals.uwbeesp32

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class DeviceTypeAdapter {
    @FromJson
    fun fromJson(type: String): DeviceType = DeviceType.fromString(type)

    @ToJson
    fun toJson(type: DeviceType): String = type.toString()
}