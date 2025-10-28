package io.moxd.mocohands_on.model.peripherals.uwbeesp32

sealed class DeviceType {
    object BasicLed : DeviceType()
    data class Unknown(val type: String) : DeviceType()

    override fun toString() = when (this) {
        BasicLed -> "uwbeesp32_basic_led"
        else -> ""
    }

    companion object {
        fun fromString(type: String) = when (type) {
            "uwbeesp32_basic_led" -> BasicLed
            else -> Unknown(type)
        }
    }
}