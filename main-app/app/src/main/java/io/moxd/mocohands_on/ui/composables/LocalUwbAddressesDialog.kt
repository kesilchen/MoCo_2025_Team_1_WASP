package io.moxd.mocohands_on.ui.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.core.uwb.UwbAddress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalUwbAddressesDialog(
    addresses: List<UwbAddress>,
    onClose: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        text = {
            addresses.forEach {
                Text(it.toString())
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