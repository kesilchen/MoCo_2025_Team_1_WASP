package io.moxd.mocohands_on

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.uwb.UwbManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.moxd.mocohands_on.model.datasource.FakeUwbRanging
import io.moxd.mocohands_on.model.datasource.RealUwbRanging
import io.moxd.mocohands_on.model.datasource.UwbRangingProvider
import io.moxd.mocohands_on.model.modifier.ExampleRangingModifier
import io.moxd.mocohands_on.ui.composables.OurScaffold
import io.moxd.mocohands_on.ui.screens.UwbConnectScreen
import io.moxd.mocohands_on.ui.screens.UwbDataScreen
import io.moxd.mocohands_on.ui.theme.MoCoHandsOnTheme
import io.moxd.mocohands_on.viewmodel.RangingViewModel
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
                val context = LocalContext.current

                var useFake by rememberSaveable { mutableStateOf(false) }

                LaunchedEffect(useFake) {
                    if (!useFake && !hasUwbPermission()) {
                        requestUwbPermission()
                    }
                }

                //val modifiers = emptyList<RangingModifier>()
                val modifiers = remember {
                    listOf(ExampleRangingModifier(factor = 10.0))
                }


                val dataSource: UwbRangingProvider = remember(useFake) {
                    if (useFake) FakeUwbRanging()
                    else RealUwbRanging(UwbManager.createInstance(context))
                }

                val vm: RangingViewModel = viewModel(
                    key = if (useFake) "ranging_vm_fake" else "ranging_vm_real",
                    factory = RangingViewModel.factory(dataSource, modifiers)
                )

                OurScaffold(
                    onNavigate = { route -> navController.navigate(route) }
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = UwbConnectRoute
                    ) {
                        composable<UwbConnectRoute> {
                            UwbConnectScreen(
                                vm = vm,
                                useFake = useFake,
                                onToggleUseFake = { newValue ->
                                    vm.onStop()
                                    if (!newValue && !hasUwbPermission()) {
                                        requestUwbPermission()
                                    }
                                    useFake = newValue
                                },
                                onNavigateToData = { navController.navigate(UwbDataRoute) }
                            )
                        }
                        composable<UwbDataRoute> {
                            UwbDataScreen(
                                vm = vm,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

sealed class Route

@Serializable
data object HomeRoute : Route()

@Serializable
data object RangingRoute : Route()
@Serializable
data object UwbConnectRoute : Route()
@Serializable
data object UwbDataRoute : Route()
