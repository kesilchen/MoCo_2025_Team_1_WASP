package io.moxd.mocohands_on.ui.composables.setup.manual

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.uwb.UwbAddress
import io.moxd.mocohands_on.ui.theme.CornerRadius
import io.moxd.mocohands_on.util.isValidMac
import io.moxd.mocohands_on.util.isValidSessionId

@Composable
fun UwbParametersStep(
    onClose: () -> Unit,
    onContinue: (name: String, uwbAddress: String, sessionId: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var macAddress by remember { mutableStateOf("") }
    var sessionId by remember { mutableStateOf("") }

    val isNameValid = name.isNotEmpty()
    val isMacValid = isValidMac(macAddress)
    val isSessionValid = isValidSessionId(sessionId)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Manual Device Setup", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Device Name") },
            placeholder = { Text("My awesome device") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = macAddress,
            onValueChange = { macAddress = it },
            label = { Text("MAC Address") },
            placeholder = { Text("AA:BB") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = !isMacValid && macAddress.isNotEmpty()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = sessionId,
            onValueChange = { sessionId = it },
            label = { Text("Session ID") },
            placeholder = { Text("42") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = !isSessionValid && sessionId.isNotEmpty(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onContinue(name, macAddress, sessionId.toInt()) },
            modifier = Modifier.fillMaxWidth(),
            enabled = isNameValid && isMacValid && isSessionValid,
            shape = RoundedCornerShape(CornerRadius)
        ) {
            Text("Next")
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onClose) {
            Text("Cancel")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UwbParametersStepPreview() {
    UwbParametersStep(onClose = {}, onContinue = { name, macAddress, sessionId -> })
}