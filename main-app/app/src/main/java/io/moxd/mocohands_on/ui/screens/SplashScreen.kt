package io.moxd.mocohands_on.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import io.moxd.mocohands_on.Route
import io.moxd.mocohands_on.UwbDataRoute
import io.moxd.mocohands_on.WelcomeRoute
import io.moxd.mocohands_on.viewmodel.SetupViewModel

@Composable
fun SplashScreen(setupViewModel: SetupViewModel = viewModel(), onNavigate: (route: Route) -> Unit) {
    val devices by setupViewModel.devices.collectAsState(null)

    LaunchedEffect(devices) {
        devices?.let {
            if (it.isEmpty()) {
                onNavigate(WelcomeRoute)
            } else {
                onNavigate(UwbDataRoute)
            }
        }
    }
}