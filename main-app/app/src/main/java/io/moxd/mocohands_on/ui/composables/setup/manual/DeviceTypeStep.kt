package io.moxd.mocohands_on.ui.composables.setup.manual

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.moxd.mocohands_on.R
import io.moxd.mocohands_on.model.peripherals.PeripheralConnectorType
import io.moxd.mocohands_on.ui.theme.CornerRadius
import io.moxd.mocohands_on.ui.theme.CornerRadiusLarge

data class DeviceTypeOption(
    val label: String,
    @DrawableRes val icon: Int
)

@Composable
fun DeviceTypeOptionItem(
    option: DeviceTypeOption,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    Card(
        onClick = onSelect,
        shape = RoundedCornerShape(CornerRadiusLarge),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
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
            Icon(
                painter = painterResource(option.icon),
                contentDescription = null,
                tint = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )

            Spacer(Modifier.width(16.dp))

            Text(
                text = option.label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DeviceTypeStep(onBack: () -> Unit, onContinue: (type: PeripheralConnectorType) -> Unit) {
    var selectedDeviceType by remember { mutableStateOf<PeripheralConnectorType?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "Select Device Type",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DeviceTypeOptionItem(
                option = DeviceTypeOption(
                    label = "UWBeEsp32",
                    icon = R.drawable.sensors_24px
                ), isSelected = selectedDeviceType == PeripheralConnectorType.UWBeEsp32
            ) { selectedDeviceType = PeripheralConnectorType.UWBeEsp32 }
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(CornerRadius)
            ) {
                Text("Back")
            }

            Button(
                onClick = {
                    selectedDeviceType?.let { type ->
                        onContinue(type)
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(CornerRadius),
                enabled = selectedDeviceType != null
            ) {
                Text("Next")
            }
        }
    }
}

@Preview
@Composable
fun DeviceTypeStepPreview() {
    DeviceTypeStep(onBack = {}, onContinue = {})
}