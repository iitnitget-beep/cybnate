package ai.cybnate.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Chat : Screen("chat", "Agent Chat", Icons.AutoMirrored.Filled.Chat)
    object Terminal : Screen("terminal", "Terminal", Icons.Filled.Terminal)
    object MiniApps : Screen("miniapps", "Mini-Apps", Icons.Filled.Apps)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}

val bottomNavItems = listOf(
    Screen.Chat,
    Screen.Terminal,
    Screen.MiniApps,
    Screen.Settings
)
