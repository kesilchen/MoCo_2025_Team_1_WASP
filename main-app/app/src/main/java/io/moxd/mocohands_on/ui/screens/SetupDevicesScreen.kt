package io.moxd.mocohands_on.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.moxd.mocohands_on.R
import io.moxd.mocohands_on.ui.composables.CardButton
import io.moxd.mocohands_on.ui.composables.setup.manual.ManualDeviceSetupBottomSheet

@Composable
fun SetupDevicesScreen() {
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
        }

    }

    if (showAddDeviceManuallyDialog) {
        ManualDeviceSetupBottomSheet(
            onClose = {showAddDeviceManuallyDialog = false},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SetupDevicesScreenPreview() {
    SetupDevicesScreen()
}