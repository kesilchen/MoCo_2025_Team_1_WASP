package io.moxd.mocohands_on.model.peripherals.uwbeesp32

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.UUID

class DeviceIdAdapter {
    @FromJson
    fun fromJson(id: String): UUID = UUID.fromString(id)

    @ToJson
    fun toJson(id: UUID): String = id.toString()
}