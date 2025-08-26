package de.thkoeln.uwbee

import android.Manifest
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import de.thkoeln.uwbee.connectivity.uwb.UwbController
import kotlinx.coroutines.launch

@Composable
fun UwbDemo() {
    val ctx = LocalContext.current
    val uwbController = UwbController(ctx)

    val coroutineScope = rememberCoroutineScope()

    PermissionWrapper(Manifest.permission.UWB_RANGING) {
        Button(onClick = {
            coroutineScope.launch {
                uwbController.prepare()
            }
        }) {
            Text("Prepare")
        }
        Button(onClick = {
            coroutineScope.launch {
                uwbController.startRanging()
            }
        }) {
            Text("Start")
        }
    }
}