package io.moxd.mocohands_on.model.ranging.uwb

import androidx.core.uwb.UwbAddress

class UwbDeviceConfiguration(val address: UwbAddress, val sessionId: Int, val sessionKey: ByteArray)