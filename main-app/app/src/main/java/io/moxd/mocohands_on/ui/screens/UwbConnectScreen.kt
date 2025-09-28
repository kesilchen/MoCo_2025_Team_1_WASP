package io.moxd.mocohands_on.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.moxd.mocohands_on.viewmodel.NewRangingViewModel

@Composable
fun UwbConnectScreen(
    vm: NewRangingViewModel,
    onNavigateToData: () -> Unit
) {
    val localUwbAddresses by vm.localUwbAddresses.collectAsState()
    var remoteUwbAddress by vm.remoteAddress

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        localUwbAddresses.map { address ->
            Text("Local address: $address")
            OutlinedTextField(
                value = remoteUwbAddress,
                onValueChange = { remoteUwbAddress = it },
                label = { Text("Destination address (e.g. 2B:7F)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Button(
            onClick = {
                vm.confirm()
                onNavigateToData()
            },
//            enabled = ui.isStartEnabled
        ) {
            Text("Start Ranging")
        }

//        val err = ui.errorMessage
//        if (!err.isNullOrBlank()) {
//            Text(err, color = MaterialTheme.colorScheme.error)
//        }
    }
}