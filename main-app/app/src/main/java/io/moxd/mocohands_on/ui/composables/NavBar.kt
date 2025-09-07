package io.moxd.mocohands_on.ui.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import io.moxd.mocohands_on.HomeRoute
import io.moxd.mocohands_on.RangingRoute
import io.moxd.mocohands_on.Route

@Composable
@Preview
fun OurNavigationBar(onNavigate: (route: Route) -> Unit = {}) {
    var currentRoute: Route by remember { mutableStateOf(HomeRoute) }
    val items = listOf(Home, Contacts)

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(icon = {
                Icon(
                    if (currentRoute == item.route) item.selectedIcon else item.unselected,
                    contentDescription = item.name
                )
            }, label = { Text(item.name) }, selected = currentRoute == item.route, onClick = {
                currentRoute = item.route
                onNavigate(item.route)
            })
        }
    }
}


sealed class BottomBarItem(
    val name: String,
    val route: Route,
    val selectedIcon: ImageVector,
    val unselected: ImageVector,
)

data object Home :
    BottomBarItem("Home", HomeRoute, Icons.Filled.Home, Icons.Outlined.Home)

data object Contacts :
    BottomBarItem("Ranging", RangingRoute, Icons.Filled.Person, Icons.Outlined.Person)