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
import io.moxd.mocohands_on.R
import io.moxd.mocohands_on.model.data.RangingReadingDto
import io.moxd.mocohands_on.model.data.RangingStateDto
import io.moxd.mocohands_on.ui.composables.BoardTarget
import io.moxd.mocohands_on.ui.composables.RangeCompass
import io.moxd.mocohands_on.viewmodel.RangingViewModel
import java.security.MessageDigest
import kotlin.math.abs

@OptIn(ExperimentalStdlibApi::class)
fun getColorFromAddress(address: UwbAddress): Color {
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(address.address)
    val hex = digest.slice(0..5).toByteArray().toHexString()
    return Color("FF$hex".toLong(16))
}

@Composable
fun UwbDataScreen(vm: RangingViewModel, onSettingsClick: () -> Unit) {
    val readings by vm.readings.collectAsState(null)
    var readingsByDevice by remember { mutableStateOf(emptyMap<String, RangingReadingDto>()) }

    LaunchedEffect(vm.readings) {
        vm.readings.collect { reading ->
            readingsByDevice = readingsByDevice.toMutableMap().apply {
                this[reading.address.toString()] = reading
            }
        }
    }
    val state by vm.state.collectAsState()

    UwbDataScreen(readings, readingsByDevice, state, onSettingsClick)
}

@Composable
fun UwbDataScreen(
    readings: RangingReadingDto?,
    readingsByDevice: Map<String, RangingReadingDto>,
    state: RangingStateDto,
    onSettingsClick: () -> Unit
) {
    val aimToleranceDeg = 10.0
    val isAimingAtBoard = readings?.azimuthDegrees?.let { abs(it) <= aimToleranceDeg } == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onSettingsClick) {
                Icon(painter = painterResource(R.drawable.settings_24px), contentDescription = null)
            }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            RangeCompass(
                targets = readingsByDevice.map {
                    BoardTarget(
                        address = it.value.address,
                        angleDegrees = it.value.azimuthDegrees,
                        elevationDegrees = it.value.elevationDegrees,
                        distanceMeters = it.value.distanceMeters,
                        color = getColorFromAddress(it.value.address)
                    )
                },
                maxRangeMeters = 3.0,
                activeIndex = 0
            )
        }

        Button(
            onClick = { /* TODO: trigger interaction */ },
            enabled = isAimingAtBoard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isAimingAtBoard) "Interact with board" else "Aim at board to interact")
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
        readings = reading,
        readingsByDevice = mapOf(Pair(reading.address.address.toString(), reading)),
        state = RangingStateDto.Running,
        onSettingsClick = {}
    )
}