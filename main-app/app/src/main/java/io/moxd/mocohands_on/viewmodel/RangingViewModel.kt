package io.moxd.mocohands_on.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.moxd.mocohands_on.model.data.RangingReadingDto
import io.moxd.mocohands_on.model.data.RangingStateDto
import io.moxd.mocohands_on.model.datasource.UwbRangingProvider
import io.moxd.mocohands_on.model.modifier.RangingModifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RangingUiState(
    val status: RangingStateDto = RangingStateDto.Idle,
    val localAddress: String = "XX:XX",
    val current: RangingReadingDto? = null,
    val destinationAddress: String = "00:00",
    val isStartEnabled: Boolean = false,
    val isStopEnabled: Boolean = false,
    val errorMessage: String? = null
)

class RangingViewModel(
    private val dataSource: UwbRangingProvider,
    private val modifiers: List<RangingModifier>
) : ViewModel() {

    private val destination = MutableStateFlow("00:00")
    private val lastReading = MutableStateFlow<RangingReadingDto?>(null)

    init {
        viewModelScope.launch {
            dataSource.readings(modifiers).collect { lastReading.value = it }
        }
    }

    val uiState: StateFlow<RangingUiState> =
        combine(dataSource.state, dataSource.localAddress, lastReading, destination) { status, local, reading, dest ->
            RangingUiState(
                status = status,
                localAddress = local,
                current = reading,
                destinationAddress = dest,
                isStartEnabled = canStart(status, dest),
                isStopEnabled = status is RangingStateDto.Running,
                errorMessage = (status as? RangingStateDto.Error)?.message
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RangingUiState()
        )

    fun onPrepare(controller: Boolean) = viewModelScope.launch {
        dataSource.prepareSession(controller)
    }

    fun onStart() = viewModelScope.launch {
        dataSource.startRanging(destination.value)
    }

    fun onStop() = viewModelScope.launch {
        dataSource.stopRanging()
    }

    fun onDestinationChanged(newValue: String) {
        destination.value = newValue.uppercase()
    }

    private fun canStart(status: RangingStateDto, dest: String): Boolean {
        if (status is RangingStateDto.Running || status is RangingStateDto.Preparing) return false
        return dest.matches(Regex("[0-9A-F]{2}:[0-9A-F]{2}"))
    }

    companion object {
        fun factory(
            dataSource: UwbRangingProvider,
            modifiers: List<RangingModifier> = emptyList()
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                RangingViewModel(dataSource, modifiers) as T
            }
        }
    }