package io.moxd.mocohands_on.model.ranging.oob

import io.moxd.mocohands_on.model.ranging.uwb.UwbDeviceConfiguration

interface ManualOobCallback {
    fun callback(uwbDeviceConfigurations: List<UwbDeviceConfiguration>)
}