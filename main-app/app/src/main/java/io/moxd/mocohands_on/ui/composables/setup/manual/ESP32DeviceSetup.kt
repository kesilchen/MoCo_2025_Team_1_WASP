package io.moxd.mocohands_on.ui.composables.setup.manual

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.moxd.mocohands_on.R
import io.moxd.mocohands_on.ui.theme.CornerRadius
import io.moxd.mocohands_on.util.isValidIpv4
import io.moxd.mocohands_on.viewmodel.SetupViewModel
import io.moxd.mocohands_on.viewmodel.TestResult
import kotlinx.coroutines.launch

@Composable
fun ESP32DeviceSetup(
    setupViewModel: SetupViewModel = viewModel<SetupViewModel>(),
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    var ipAddress by remember { mutableStateOf("") }
    var isConnecting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<TestResult?>(null) }

    val isValidIp = isValidIpv4(ipAddress)
    val isContinueEnabled = testResult == TestResult.Success

    val scope = rememberCoroutineScope()


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            "Connect to your ESP32",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = ipAddress,
            onValueChange = {
                ipAddress = it
                testResult = null
            },
            label = { Text("IP Address") },
            placeholder = { Text("192.168.0.123") },
            isError = ipAddress.isNotEmpty() && !isValidIp,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        if (!isValidIp && ipAddress.isNotEmpty()) {
            Text(
                "Invalid IP format",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                isConnecting = true
                testResult = null
                scope.launch {
                    testResult = setupViewModel.testEsp32Connection(ipAddress)
                    isConnecting = false
                }
            },
            enabled = isValidIp && !isConnecting,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(CornerRadius)
        ) {
            if (isConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Connecting...")
            } else {
                Text("Test Connection")
            }
        }

        Spacer(Modifier.height(16.dp))

        when (val result = testResult) {
            is TestResult.Success -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.check_circle_24px),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Connection successful!", color = MaterialTheme.colorScheme.primary)
                }
            }

            is TestResult.Error -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.error_24px),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(result.message, color = MaterialTheme.colorScheme.error)
                }
            }

            null -> Unit
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
                onClick = onContinue,
                enabled = isContinueEnabled,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(CornerRadius)
            ) {
                Text("Continue")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ESP32DeviceSetupPreview() {
    ESP32DeviceSetup(onBack = {}, onContinue = {})
}