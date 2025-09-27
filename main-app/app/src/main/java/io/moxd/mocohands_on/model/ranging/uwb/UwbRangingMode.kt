package io.moxd.mocohands_on.model.ranging.uwb

import androidx.core.uwb.RangingParameters

enum class UwbRangingMode(val uwbConfig: Int) {
    Unicast(RangingParameters.CONFIG_UNICAST_DS_TWR),
    Multicast(RangingParameters.CONFIG_MULTICAST_DS_TWR)
}