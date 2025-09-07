package io.moxd.mocohands_on.model.data

sealed class RangingStateDto {
    data object Idle : RangingStateDto()
    data object Preparing : RangingStateDto()
    data class Ready(val localAddress: String? = null) : RangingStateDto()
    data object Running : RangingStateDto()
    data object Stopped : RangingStateDto()
    data class Error(val message: String) : RangingStateDto()
}