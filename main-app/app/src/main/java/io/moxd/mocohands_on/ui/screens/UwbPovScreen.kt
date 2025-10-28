package io.moxd.mocohands_on.ui.screens

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import io.moxd.mocohands_on.BuildConfig
import io.moxd.mocohands_on.model.data.RangingReadingDto
import io.moxd.mocohands_on.model.data.RangingStateDto
import io.moxd.mocohands_on.ui.composables.BoardTarget
import io.moxd.mocohands_on.ui.composables.LocalUwbAddressesDialog
import io.moxd.mocohands_on.ui.composables.RangingPovView
import io.moxd.mocohands_on.viewmodel.RangingViewModel
import io.moxd.mocohands_on.viewmodel.SetupViewModel

@Composable
fun UwbPovScreen(vm: RangingViewModel, setupViewModel: SetupViewModel = viewModel()) {
    var readingsByDevice by remember { mutableStateOf(emptyMap<String, RangingReadingDto>()) }
    var lastState by remember { mutableStateOf<RangingStateDto?>(null) }
    var skipNextEmission by remember { mutableStateOf(false) }
    val state by vm.state.collectAsState()

    fun resetTargets(skipReplay: Boolean) {
        readingsByDevice = emptyMap()
        skipNextEmission = skipReplay
    }

    var showLocalUwbAddressesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.start()
        if (!BuildConfig.USE_FAKE_DATA) {
            showLocalUwbAddressesDialog = true
        }
    }

    LaunchedEffect(Unit) {
        resetTargets(vm.readings.replayCache.isNotEmpty())
    }

    LaunchedEffect(state) {
        if (state is RangingStateDto.Running && lastState !is RangingStateDto.Running) {
            resetTargets(vm.readings.replayCache.isNotEmpty())
        }

        if (state !is RangingStateDto.Running) {
            resetTargets(false)
        }

        lastState = state
    }

    LaunchedEffect(vm.readings) {
        vm.readings.collect { reading ->
            if (skipNextEmission) {
                skipNextEmission = false
                return@collect
            }
            readingsByDevice = readingsByDevice.toMutableMap().apply {
                this[reading.address.toString()] = reading
            }
        }
    }
    val localUwbAddresses by vm.localUwbAddresses.collectAsState()

    val devices by setupViewModel.devices.collectAsState(listOf())

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }

    Box {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        ) { view ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = view.surfaceProvider
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context))
        }
        RangingPovView(targets = readingsByDevice.map {
            BoardTarget(
                address = it.value.address,
                angleDegrees = it.value.azimuthDegrees,
                elevationDegrees = it.value.elevationDegrees,
                distanceMeters = it.value.distanceMeters,
                color = getColorFromAddress(it.value.address)
            )
        }, readings = vm.readings, devices = devices, onInteract = { target ->
            if (target != null) {
                setupViewModel.setEsp32LedStatus(target.address, true)
            }
        })
    }
    if (showLocalUwbAddressesDialog) {
        LocalUwbAddressesDialog(localUwbAddresses, devices, onClose = {
            showLocalUwbAddressesDialog = false
        }, onConfirm = {
            showLocalUwbAddressesDialog = false
            vm.confirm()
        })
    }
}