package io.moxd.mocohands_on.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions // Import KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import io.moxd.mocohands_on.viewmodel.RangingViewModel
import io.moxd.mocohands_on.model.data.RangingStateDto

@Composable
fun UwbConnectScreen(
    vm: RangingViewModel,
    onNavigateToData: () -> Unit
) {
    val ui by vm.uiState.collectAsState()

    var isController by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Controller Toggle + Prepare
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Controller")
            Spacer(Modifier.width(8.dp))
            Switch(checked = isController, onCheckedChange = { isController = it })
        }

        Button(
            onClick = { vm.onPrepare(controller = isController) },
            enabled = ui.status !is RangingStateDto.Preparing
        ) {
            Text("Prepare Session")
        }

        // Local Address
        Text("Local address: ${ui.localAddress}")

        // Destination Address Eingabe
        OutlinedTextField(
            value = ui.destinationAddress,
            onValueChange = { vm.onDestinationChanged(it) },
            label = { Text("Destination address (e.g. 2B:7F)") },
            singleLine = true, // Remove KeyboardOptions composable function
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Start Button → Navigiert zum Daten-Screen
        Button(
            onClick = {
                vm.onStart()
                onNavigateToData()
            },
            enabled = ui.isStartEnabled
        ) {
            Text("Start Ranging")
        }

        // Fehleranzeige (falls vorhanden)
        val err = ui.errorMessage
        if (!err.isNullOrBlank()) {
            Text(err, color = MaterialTheme.colorScheme.error)
        }
    }
}