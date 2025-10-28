package io.moxd.mocohands_on.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.uwb.UwbAddress
import androidx.lifecycle.viewmodel.compose.viewModel
import io.moxd.mocohands_on.BuildConfig
import io.moxd.mocohands_on.R
import io.moxd.mocohands_on.model.data.RangingReadingDto
import io.moxd.mocohands_on.model.data.RangingStateDto
import io.moxd.mocohands_on.model.database.entities.DeviceWithPeripheralConnector
import io.moxd.mocohands_on.ui.composables.BoardTarget
import io.moxd.mocohands_on.ui.composables.LocalUwbAddressesDialog
import io.moxd.mocohands_on.ui.composables.RangeCompass
import io.moxd.mocohands_on.viewmodel.RangingViewModel
import io.moxd.mocohands_on.viewmodel.SetupViewModel
import java.security.MessageDigest
import kotlin.math.abs

private val DarkModePalette = listOf(
    Color(0xFFFFB300),
    Color(0xFFFF7043),
    Color(0xFFF06292),
    Color(0xFFBA68C8),
    Color(0xFF9575CD),
    Color(0xFF4FC3F7),
    Color(0xFF4DB6AC),
    Color(0xFF81C784),
    Color(0xFFFFF176),
    Color(0xFFFFD54F),
    Color(0xFFAED581),
    Color(0xFF64B5F6)
)

@OptIn(ExperimentalStdlibApi::class)
fun getColorFromAddress(address: UwbAddress): Color {
    val digest = MessageDigest
        .getInstance("SHA-256")
        .digest(address.address)

    val hash = digest.take(4).fold(0) { accumulator, byte ->
        (accumulator shl 8) or (byte.toInt() and 0xFF)
    }

    val paletteIndex = (hash and Int.MAX_VALUE) % DarkModePalette.size
    return DarkModePalette[paletteIndex]
}

@Composable
fun UwbDataScreen(
    vm: RangingViewModel,
    setupViewModel: SetupViewModel = viewModel(),
    onSettingsClick: () -> Unit
) {
    var readingsByDevice by remember { mutableStateOf(emptyMap<String, RangingReadingDto>()) }
    var lastState by remember { mutableStateOf<RangingStateDto?>(null) }
    var skipNextEmission by remember { mutableStateOf(false) }
    val state by vm.state.collectAsState()

    fun resetTargets(skipReplay: Boolean) {
        readingsByDevice = emptyMap()
        skipNextEmission = skipReplay
    }

    var showLocalUwbAddressesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.start()
        if (!BuildConfig.USE_FAKE_DATA) {
            showLocalUwbAddressesDialog = true
        }
    }

    LaunchedEffect(Unit) {
        resetTargets(vm.readings.replayCache.isNotEmpty())
    }

    LaunchedEffect(state) {
        if (state is RangingStateDto.Running && lastState !is RangingStateDto.Running) {
            resetTargets(vm.readings.replayCache.isNotEmpty())
        }

        if (state !is RangingStateDto.Running) {
            resetTargets(false)
        }

        lastState = state
    }

    LaunchedEffect(vm.readings) {
        vm.readings.collect { reading ->
            if (skipNextEmission) {
                skipNextEmission = false
                return@collect
            }
            readingsByDevice = readingsByDevice.toMutableMap().apply {
                this[reading.address.toString()] = reading
            }
        }
    }
    val localUwbAddresses by vm.localUwbAddresses.collectAsState()

    val devices by setupViewModel.devices.collectAsState(listOf())

    UwbDataScreen(readingsByDevice, devices, onSettingsClick, onInteract = { address ->
        setupViewModel.setEsp32LedStatus(address, true)
    })

    if (showLocalUwbAddressesDialog) {
        LocalUwbAddressesDialog(localUwbAddresses, devices, onClose = {
            showLocalUwbAddressesDialog = false
        }, onConfirm = {
            showLocalUwbAddressesDialog = false
            vm.confirm()
        })
    }
}

@Composable
fun UwbDataScreen(
    readingsByDevice: Map<String, RangingReadingDto>,
    devices: List<DeviceWithPeripheralConnector>,
    onSettingsClick: () -> Unit,
    onInteract: (address: UwbAddress) -> Unit
) {
    val targets = readingsByDevice.values.map { rr ->
        BoardTarget(
            address = rr.address,
            angleDegrees = rr.azimuthDegrees,
            elevationDegrees = rr.elevationDegrees,
            distanceMeters = rr.distanceMeters,
            color = getColorFromAddress(rr.address)
        )
    }

    val aimToleranceDeg = 10.0

    val activeIndex = targets
        .withIndex()
        .filter { it.value.angleDegrees != null }
        .filter { abs(it.value.angleDegrees!!) <= aimToleranceDeg }
        .minWithOrNull(
            compareBy(
                { abs(it.value.elevationDegrees ?: Double.POSITIVE_INFINITY) },
                { abs(it.value.angleDegrees!!) }
            )
        )
        ?.index

    val isAimingAtBoard = activeIndex != null
    val chosen = activeIndex?.let { targets[it] }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onSettingsClick) {
                Icon(painter = painterResource(R.drawable.settings_24px), contentDescription = null)
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RangeCompass(
                targets = targets,
                maxRangeMeters = 3.0,
                activeIndex = activeIndex
            )
        }
        Button(
            onClick = {
                if (chosen != null) {
                    onInteract(chosen.address)
                }
            },
            enabled = isAimingAtBoard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isAimingAtBoard) "Interact with ${devices.find { it.device.uwbAddress == chosen?.address.toString() }?.device?.name ?: "board"}" else "Aim at board to interact")
        }
    }

}

@Preview(showBackground = true)
@Composable
fun UwbDataScreenPreview() {
    val reading = RangingReadingDto(
        address = UwbAddress("12:34"),
        distanceMeters = 1.2,
        azimuthDegrees = 30.5,
        elevationDegrees = 4.2,
        measurementTimeMillis = 1000
    )

    UwbDataScreen(
        readingsByDevice = mapOf(Pair(reading.address.address.toString(), reading)),
        devices = listOf(),
        onSettingsClick = {},
        onInteract = {}
    )
}