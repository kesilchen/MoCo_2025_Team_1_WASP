package io.moxd.mocohands_on.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.moxd.mocohands_on.model.data.RangingStateDto
import io.moxd.mocohands_on.viewmodel.NewRangingViewModel

@Composable
fun UwbConnectScreen(
    vm: NewRangingViewModel,
    onNavigateToData: () -> Unit
) {
    val localUwbAddresses by vm.localUwbAddresses.collectAsState()
    val remoteUwbAddresses = vm.remoteAddresses
    val devices by vm.devices.collectAsState()
    val state by vm.state.collectAsState()

    var numberOfDevices by rememberSaveable { mutableStateOf("2") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            OutlinedTextField(
                value = numberOfDevices,
                onValueChange = {
                    numberOfDevices = it
                    if (it.toIntOrNull() != null) {
                        vm.setNumberOfDevices(it.toInt())
                    }
                },
                label = { Text("Number of remotes") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        val parsedNumberOfDevices = numberOfDevices.toIntOrNull()
        parsedNumberOfDevices?.let {
            repeat(it) { index ->
                Column {
                    Text("Local address: ${if (localUwbAddresses.size > index) localUwbAddresses[index] else "xx:xx"}")
                    Text("Session ID: ${42 + index}")
                    OutlinedTextField(
                        value = remoteUwbAddresses[index],
                        onValueChange = { vm.updateRemoteAddress(index, it) },
                        label = { Text("Destination address (e.g. 2B:7F)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Button(
            onClick = {
                vm.confirm()
                onNavigateToData()
            },
            enabled = remoteUwbAddresses.all { remoteAddress -> remoteAddress.matches(Regex("[0-9A-F]{2}:[0-9A-F]{2}")) } && state is RangingStateDto.Preparing
        ) {
            Text("Start Ranging")
        }

//        val err = ui.errorMessage
//        if (!err.isNullOrBlank()) {
//            Text(err, color = MaterialTheme.colorScheme.error)
//        }
    }
}