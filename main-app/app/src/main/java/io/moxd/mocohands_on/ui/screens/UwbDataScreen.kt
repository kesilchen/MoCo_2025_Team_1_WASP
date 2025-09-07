package io.moxd.mocohands_on.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.moxd.mocohands_on.model.data.RangingStateDto
import io.moxd.mocohands_on.viewmodel.RangingViewModel

@Composable
fun UwbDataScreen(
    vm: RangingViewModel,
    onBack: () -> Unit
) {
    val ui by vm.uiState.collectAsState()
    val reading = ui.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Status: ${ui.status.javaClass.simpleName}")

        Text("Distance: ${reading?.distanceMeters?.let { "%.2f m".format(it) } ?: "-"}")
        Text("Azimuth:  ${reading?.azimuthDeg?.let { "%.1f°".format(it) } ?: "-"}")
        Text("Elevation:${reading?.elevationDeg?.let { "%.1f°".format(it) } ?: "-"}")

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    vm.onStop()
                    onBack()
                },
                enabled = ui.isStopEnabled
            ) { Text("Stop & Back") }

            // Optional: nochmal neu vorbereiten ohne zurückzugehen
            if (ui.status !is RangingStateDto.Running) {
                Button(onClick = { vm.onPrepare(controller = true) }) {
                    Text("Re-Prepare")
                }
            }
        }

        val err = ui.errorMessage
        if (!err.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(err, color = MaterialTheme.colorScheme.error)
        }
    }
}