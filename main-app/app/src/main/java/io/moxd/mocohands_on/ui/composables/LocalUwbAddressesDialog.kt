package io.moxd.mocohands_on.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.core.uwb.UwbAddress
import io.moxd.mocohands_on.model.database.entities.DeviceWithPeripheralConnector
import io.moxd.mocohands_on.model.ranging.uwb.UwbDeviceConfiguration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalUwbAddressesDialog(
    addresses: List<UwbAddress>,
    devices: List<DeviceWithPeripheralConnector>,
    onClose: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        text = {
            Column {
                addresses.forEachIndexed { index, addr ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        val remoteDevice = devices[index]
                        Text("${remoteDevice.device.name} (${remoteDevice.device.uwbAddress}): ")
                        Text(addr.toString())
                    }
                }
            }
        },
        onDismissRequest = {
            onClose()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                }
            ) {
                Text("Confirm")
            }
        }
    )
}