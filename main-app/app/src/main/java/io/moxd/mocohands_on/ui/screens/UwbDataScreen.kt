package io.moxd.mocohands_on.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.moxd.mocohands_on.model.data.RangingStateDto
import io.moxd.mocohands_on.ui.composables.BoardTarget
import io.moxd.mocohands_on.ui.composables.RangeCompass
import io.moxd.mocohands_on.viewmodel.RangingViewModel
import kotlin.math.abs

@Composable
fun UwbDataScreen(
    vm: RangingViewModel,
    vm2: RangingViewModel,
    onBack: () -> Unit
) {
    val ui by vm.uiState.collectAsState()
    val ui2 by vm2.uiState.collectAsState()
    val currentRangingData = ui.current
    val currentRangingData2 = ui2.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row {
            Column {
                Text("Status: ${ui.status.javaClass.simpleName}")

                Text("Distance: ${currentRangingData?.distanceMeters?.let { "%.2f m".format(it) } ?: "-"}")
                Text("Azimuth:  ${currentRangingData?.azimuthDeg?.let { "%.1f°".format(it) } ?: "-"}")
                Text("Elevation:${currentRangingData?.elevationDeg?.let { "%.1f°".format(it) } ?: "-"}")
            }

            Column(modifier = Modifier.padding(start = 80.dp)) {
                Text("Status: ${ui2.status.javaClass.simpleName}")

                Text("Distance: ${currentRangingData2?.distanceMeters?.let { "%.2f m".format(it) } ?: "-"}")
                Text("Azimuth:  ${currentRangingData2?.azimuthDeg?.let { "%.1f°".format(it) } ?: "-"}")
                Text("Elevation:${currentRangingData2?.elevationDeg?.let { "%.1f°".format(it) } ?: "-"}")
            }
        }

        Spacer(Modifier.height(4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    vm.onStop()
                    vm2.onStop()
                    onBack()
                },
                enabled = ui.isStopEnabled
            ) { Text("Stop & Back") }

            if (ui.status !is RangingStateDto.Running) {
                Button(onClick = {
                    vm.onPrepare(controller = true)
                    vm2.onPrepare(controller = true)
                }) {
                    Text("Re-Prepare")
                }
            }
        }

        val err = ui.errorMessage
        if (!err.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(err, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(4.dp))

        RangeCompass(
            targets = listOf(
                BoardTarget(
                    angleDegrees = currentRangingData?.azimuthDeg,
                    distanceMeters = currentRangingData?.distanceMeters
                ),
                BoardTarget(
                    angleDegrees = currentRangingData2?.azimuthDeg,
                    distanceMeters = currentRangingData2?.distanceMeters,
                    color = Color.Green
                )
            ),
            maxRangeMeters = 3.0,
            activeIndex = 0
        )

        Spacer(Modifier.height(4.dp))

        val aimToleranceDeg = 10.0
        val isAimingAtBoard =
            currentRangingData?.azimuthDeg?.let { abs(it) <= aimToleranceDeg } == true

        Button(
            onClick = { /* TODO: trigger interaction */ },
            enabled = isAimingAtBoard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isAimingAtBoard) "Interact with board" else "Aim at board to interact")
        }
    }
}