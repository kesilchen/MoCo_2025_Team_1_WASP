package io.moxd.mocohands_on.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.moxd.mocohands_on.R
import io.moxd.mocohands_on.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OurScaffold(
    onNavigate: (route: Route) -> Unit = {},
    fapClick: () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.app_name)) },
        )
    }, bottomBar = {
        OurNavigationBar(onNavigate)
    }) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            content()
        }
    }
}

@Composable
@Preview
fun OurScaffoldPreview() {
    OurScaffold {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Magenta)
        ) {
            Text(
                "our content goes here",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .align(Alignment.Center)
                    .rotate(-45f)
            )
        }
    }
}