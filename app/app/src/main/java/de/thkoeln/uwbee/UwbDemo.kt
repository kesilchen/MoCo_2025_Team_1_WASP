package de.thkoeln.uwbee

import android.Manifest
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.uwb.UwbManager
import de.thkoeln.uwbee.connectivity.uwb.UwbController
import kotlinx.coroutines.launch

@Composable
fun UwbDemo() {
    val ctx = LocalContext.current
    val uwbManager = UwbManager.createInstance(ctx)
    val uwbController = UwbController(
        "01:01",
        42,
        uwbManager,
        byteArrayOf(0x08, 0x07, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06),
    )
    val uwbController2 = UwbController(
        "02:02",
        43,
        uwbManager,
        byteArrayOf(0x08, 0x07, 0x01, 0x02, 0x03, 0x04, 0x05, 0x07),
    )

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
        Button(onClick = {
            coroutineScope.launch {
                uwbController.addControlee()
            }
        }) {
            Text("Add Controlee")
        }
        Button(onClick = {
            coroutineScope.launch {
                uwbController2.prepare()
            }
        }) {
            Text("Prepare")
        }
        Button(onClick = {
            coroutineScope.launch {
                uwbController2.startRanging()
            }
        }) {
            Text("Start")
        }
    }
}