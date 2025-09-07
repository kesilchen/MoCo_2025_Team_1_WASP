package io.moxd.mocohands_on

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.uwb.UwbManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.moxd.mocohands_on.model.datasource.RealUwbRanging
import io.moxd.mocohands_on.model.datasource.UwbRangingProvider
import io.moxd.mocohands_on.ui.composables.OurScaffold
import io.moxd.mocohands_on.ui.screens.HomeScreen
import io.moxd.mocohands_on.ui.screens.RangingScreen
import io.moxd.mocohands_on.ui.screens.UwbConnectScreen
import io.moxd.mocohands_on.ui.screens.UwbDataScreen
import io.moxd.mocohands_on.ui.theme.MoCoHandsOnTheme
import io.moxd.mocohands_on.viewmodel.RangingViewModel
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoCoHandsOnTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val dataSource: UwbRangingProvider = remember {
                    RealUwbRanging(UwbManager.createInstance(context))
                }
                val vm: RangingViewModel = viewModel(
                    key = "ranging_vm",
                    factory = RangingViewModel.factory(dataSource)
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
                                onNavigateToData = { navController.navigate(UwbDataRoute) }
                            )
                        }
                        composable<UwbDataRoute> {
                            UwbDataScreen(
                                vm = vm,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable<RangingRoute> {
                            RangingScreen()
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
