package io.moxd.mocohands_on.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.uwb.UwbAddress
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
fun UwbDataScreen(
    vm: RangingViewModel,
    onBack: () -> Unit
) {
    val readings by vm.readings.collectAsState(null)
    var readingsByDevice by remember { mutableStateOf(emptyMap<String, RangingReadingDto>()) }
    val state by vm.state.collectAsState()

    LaunchedEffect(vm.readings) {
        vm.readings.collect {reading ->
            readingsByDevice = readingsByDevice.toMutableMap().apply {
                this[reading.address.toString()] = reading
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Status: ${state.javaClass.simpleName}")

//        Text("Distance: ${readings?.distanceMeters?.let { "%.2f m".format(it) } ?: "-"}")
//        Text("Azimuth:  ${readings?.azimuthDegrees?.let { "%.1f°".format(it) } ?: "-"}")
//        Text("Elevation:${readings?.elevationDegrees?.let { "%.1f°".format(it) } ?: "-"}")

        Spacer(Modifier.height(4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    vm.restart()
                    onBack()
                },
                enabled = state is RangingStateDto.Running
            ) { Text("Stop & Back") }

//            if (state !is RangingStateDto.Running) {
//                Button(onClick = {
//                    vm.onPrepare(controller = true)
//                }) {
//                    Text("Re-Prepare")
//                }
//            }
        }

//        val err = ui.errorMessage
//        if (!err.isNullOrBlank()) {
//            Spacer(Modifier.height(4.dp))
//            Text(err, color = MaterialTheme.colorScheme.error)
//        }

        Spacer(Modifier.height(4.dp))

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

        Spacer(Modifier.height(4.dp))

        val aimToleranceDeg = 10.0
        val isAimingAtBoard = readings?.azimuthDegrees?.let { abs(it) <= aimToleranceDeg } == true

        Button(
            onClick = { /* TODO: trigger interaction */ },
            enabled = isAimingAtBoard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isAimingAtBoard) "Interact with board" else "Aim at board to interact")
        }
    }
}