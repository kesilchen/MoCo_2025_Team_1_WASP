package io.moxd.mocohands_on.ui.composables.setup.manual

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualDeviceSetupBottomSheet(onClose: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

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
                    onContinue = { navController.navigate(DeviceTypeRoute) },
                )
            }
            composable<DeviceTypeRoute> {
                DeviceTypeStep(onBack = { navController.popBackStack() }, onContinue = {
                    navController.navigate(
                        ESP32DeviceSetupRoute
                    )
                })
            }
            composable<ESP32DeviceSetupRoute> {
                ESP32DeviceSetup(onBack = { navController.popBackStack() }, onContinue = onClose)
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
