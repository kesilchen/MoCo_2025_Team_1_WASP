package de.thkoeln.uwbee

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun PermissionWrapper(permission: String, content: @Composable () -> Unit) {
    val context = LocalContext.current
    var hasUwbPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            hasUwbPermission = isGranted
        }

    LaunchedEffect(Unit) {
        if(!hasUwbPermission) {
            requestPermissionLauncher.launch(permission)
        }
    }

    if (hasUwbPermission) {
        content()
    }
}