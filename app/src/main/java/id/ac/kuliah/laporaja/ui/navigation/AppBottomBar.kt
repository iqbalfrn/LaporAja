package id.ac.kuliah.laporaja.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

private data class BottomTab(val route: String, val label: String, val outline: ImageVector, val filled: ImageVector)

private val tabs = listOf(
    BottomTab(Routes.HOME, "Beranda", Icons.Outlined.Home, Icons.Filled.Home),
    BottomTab(Routes.REPORTS, "Laporan", Icons.AutoMirrored.Outlined.Article, Icons.AutoMirrored.Filled.Article),
    BottomTab(Routes.ACTIVITIES, "Kegiatan", Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth),
    BottomTab(Routes.ACCOUNT, "Akun", Icons.Outlined.Person, Icons.Filled.Person),
)

/** Empat tab tetap, hanya dirender pada rute S3, S4, S10, S13 (Subbab 5.1). */
@Composable
fun AppBottomBar(
    selectedRoute: String,
    onSelectRoute: (String) -> Unit,
) {
    NavigationBar {
        tabs.forEach { tab ->
            val selected = tab.route == selectedRoute
            NavigationBarItem(
                selected = selected,
                onClick = { onSelectRoute(tab.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) tab.filled else tab.outline,
                        contentDescription = tab.label,
                    )
                },
                label = { Text(tab.label) },
            )
        }
    }
}
