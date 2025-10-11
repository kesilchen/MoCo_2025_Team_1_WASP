package io.moxd.mocohands_on.ui.composables.setup.manual

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.uwb.UwbAddress
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.moxd.mocohands_on.model.peripherals.PeripheralConnectorType
import io.moxd.mocohands_on.viewmodel.SetupViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualDeviceSetupBottomSheet(
    setupViewModel: SetupViewModel = viewModel(),
    onClose: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    var name by remember { mutableStateOf<String?>(null) }
    var uwbAddress by remember { mutableStateOf<String?>(null) }
    var uwbSessionId by remember { mutableStateOf<Int?>(null) }
    var peripheralConnectorType by remember { mutableStateOf<PeripheralConnectorType?>(null) }
    var peripheralConnectorApiUrl by remember { mutableStateOf<String?>(null) }

    fun finishSetup() {
        val newName = name
        val newUwbAddress = uwbAddress
        val newUwbSessionId = uwbSessionId
        val newPeripheralConnectorType = peripheralConnectorType
        val newPeripheralConnectorApiUrl = peripheralConnectorApiUrl
        if (newName != null && newUwbAddress != null && newUwbSessionId != null && newPeripheralConnectorType != null && newPeripheralConnectorApiUrl != null) {
            setupViewModel.createDevice(
                name = newName,
                uwbAddress = newUwbAddress,
                uwbSessionId = newUwbSessionId,
                peripheralType = newPeripheralConnectorType,
                peripheralApiUrl = newPeripheralConnectorApiUrl,
            )
            onClose()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
    ) {
        NavHost(
            navController = navController, startDestination = UwbParametersRoute,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { 1000 },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -1000 },
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -1000 },
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { 1000 },
                    animationSpec = tween(300)
                )
            }) {
            composable<UwbParametersRoute> {
                UwbParametersStep(
                    onClose = {
                        scope.launch {
                            sheetState.hide()
                            onClose()
                        }
                    },
                    onContinue = { newName, newMacAddress, newSessionId ->
                        name = newName
                        uwbAddress = newMacAddress
                        uwbSessionId = newSessionId
                        navController.navigate(DeviceTypeRoute)
                    },
                )
            }
            composable<DeviceTypeRoute> {
                DeviceTypeStep(onBack = { navController.popBackStack() }, onContinue = { type ->
                    peripheralConnectorType = type
                    navController.navigate(
                        ESP32DeviceSetupRoute
                    )
                })
            }
            composable<ESP32DeviceSetupRoute> {
                ESP32DeviceSetup(
                    onBack = { navController.popBackStack() },
                    onContinue = { ipAddress ->
                        peripheralConnectorApiUrl = "http://$ipAddress/"
                        finishSetup()
                    }
                )
            }
        }
    }
}

sealed class Route

@Serializable
data object UwbParametersRoute : Route()

@Serializable
data object DeviceTypeRoute : Route()

@Serializable
data object ESP32DeviceSetupRoute : Route()
