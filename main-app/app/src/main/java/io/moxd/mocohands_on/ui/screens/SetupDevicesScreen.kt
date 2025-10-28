package io.moxd.mocohands_on.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.moxd.mocohands_on.R
import io.moxd.mocohands_on.model.database.entities.Device
import io.moxd.mocohands_on.model.database.entities.DeviceWithPeripheralConnector
import io.moxd.mocohands_on.model.database.entities.PeripheralConnector
import io.moxd.mocohands_on.model.peripherals.PeripheralConnectorType
import io.moxd.mocohands_on.ui.composables.CardButton
import io.moxd.mocohands_on.ui.composables.setup.manual.ManualDeviceSetupBottomSheet
import io.moxd.mocohands_on.ui.theme.CornerRadius
import io.moxd.mocohands_on.ui.theme.CornerRadiusLarge
import io.moxd.mocohands_on.viewmodel.SetupViewModel

@Composable
fun SetupDevicesScreen(setupViewModel: SetupViewModel = viewModel(), onContinue: () -> Unit) {
    val devices by setupViewModel.devices.collectAsState(listOf())

    SetupDevicesScreen(devices, onContinue = onContinue)
}

@Composable
fun SetupDevicesScreen(devices: List<DeviceWithPeripheralConnector>, onContinue: () -> Unit) {
    var showAddDeviceManuallyDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(
                10.dp,
                alignment = Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Let's add some devices",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            CardButton(
                title = "Discover nearby devices",
                description = "Use Bluetooth Low Energy to automatically detect compatible devices nearby",
                icon = R.drawable.sensors_24px
            ) {}
            CardButton(
                title = "Add devices manually",
                description = "Enter the connection details of the device manually",
                icon = R.drawable.library_add_24px
            ) {
                showAddDeviceManuallyDialog = true
            }

            if (devices.isNotEmpty()) {
                HorizontalDivider()

                devices.forEach {
                    Card(
                        shape = RoundedCornerShape(CornerRadiusLarge),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = CardDefaults.cardColors(
                            MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = it.device.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = it.device.uwbAddress,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = it.peripheralConnector.type.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(CornerRadius),
            onClick = onContinue,
            enabled = devices.isNotEmpty()
        ) {
            Text("Continue", style = MaterialTheme.typography.labelLarge)
            Icon(
                painter = painterResource(R.drawable.chevron_right_24px),
                contentDescription = null
            )
        }
    }

    if (showAddDeviceManuallyDialog) {
        ManualDeviceSetupBottomSheet(
            onClose = { showAddDeviceManuallyDialog = false },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SetupDevicesScreenPreview() {
    SetupDevicesScreen(
        listOf(
            DeviceWithPeripheralConnector(
                device = Device(
                    id = 0,
                    name = "My awesome device",
                    uwbAddress = "12:34",
                    uwbSessionId = 42,
                    peripheralConnectorId = 0,
                ),
                peripheralConnector = PeripheralConnector(
                    id = 0,
                    type = PeripheralConnectorType.UWBeEsp32,
                    apiUrl = "http://192.168.137.69/"
                )
            )
        ),
        onContinue = {}
    )
}