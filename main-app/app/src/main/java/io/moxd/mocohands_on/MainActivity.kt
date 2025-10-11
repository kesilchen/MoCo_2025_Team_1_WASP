package io.moxd.mocohands_on

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import io.moxd.mocohands_on.ui.composables.OurScaffold
import io.moxd.mocohands_on.ui.screens.SetupDevicesScreen
import io.moxd.mocohands_on.ui.screens.SplashScreen
import io.moxd.mocohands_on.ui.screens.UwbConnectScreen
import io.moxd.mocohands_on.ui.screens.UwbDataScreen
import io.moxd.mocohands_on.ui.screens.UwbPovScreen
import io.moxd.mocohands_on.ui.screens.WelcomeScreen
import io.moxd.mocohands_on.ui.theme.MoCoHandsOnTheme
import io.moxd.mocohands_on.viewmodel.RangingViewModel
import io.moxd.mocohands_on.viewmodel.SetupViewModel
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {

    private val uwbPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private fun hasUwbPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this, Manifest.permission.UWB_RANGING
        ) == PackageManager.PERMISSION_GRANTED

    private fun requestUwbPermission() {
        uwbPermissionLauncher.launch(Manifest.permission.UWB_RANGING)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MoCoHandsOnTheme {
                val navController = rememberNavController()

                val useFake = BuildConfig.USE_FAKE_DATA
                val showDebugScreen = BuildConfig.SHOW_DEBUG_SCREEN

                LaunchedEffect(useFake) {
                    if (!useFake && !hasUwbPermission()) {
                        requestUwbPermission()
                    }
                }

                val rangingViewModel = viewModel<RangingViewModel>(
                    factory = RangingViewModel.factory(
                        LocalContext.current.applicationContext as Application
                    )
                )

                val setupViewModel = viewModel<SetupViewModel>()

                val devices by setupViewModel.devices.collectAsState(listOf())

                OurScaffold(
                    onNavigate = { route -> navController.navigate(route) }
                ) {
                    NavHost(
                        navController = navController,
                        startDestination =
//                            if (showDebugScreen) UwbConnectRoute else UwbDataRoute
                            SplashRoute
                    ) {
                        composable<SplashRoute> {
                            SplashScreen { route ->
                                navController.navigate(route)
                            }
                        }
                        composable<WelcomeRoute> {
                            WelcomeScreen {
                                navController.navigate(SetupDevicesRoute)
                            }
                        }
                        composable<SetupDevicesRoute> {
                            SetupDevicesScreen {
                                navController.navigate(UwbDataRoute) {
                                    popUpTo(0) {
                                        inclusive = true
                                    }
                                }
                            }
                        }
                        composable<UwbConnectRoute> {
                            UwbConnectScreen(
                                vm = rangingViewModel,
                                onNavigateToData = { navController.navigate(UwbDataRoute) }
                            )
                        }
                        composable<UwbDataRoute> {
                            when (BuildConfig.VISUALIZATION_TYPE) {
                                "COMPASS" -> UwbDataScreen(
                                    vm = rangingViewModel,
                                    onSettingsClick = {
                                        navController.navigate(SetupDevicesRoute)
                                    }
                                )

                                "POV" ->
                                    UwbPovScreen(vm = rangingViewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed class Route

@Serializable
data object SplashRoute : Route()

@Serializable
data object WelcomeRoute : Route()

@Serializable
data object SetupDevicesRoute : Route()

@Serializable
data object HomeRoute : Route()

@Serializable
data object RangingRoute : Route()

@Serializable
data object UwbConnectRoute : Route()

@Serializable
data object UwbDataRoute : Route()
