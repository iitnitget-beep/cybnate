package ai.cybnate

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.cybnate.data.model.MiniApp
import ai.cybnate.service.TerminalForegroundService
import ai.cybnate.ui.chat.ChatScreen
import ai.cybnate.ui.chat.ChatViewModel
import ai.cybnate.ui.miniapps.MiniAppWebViewer
import ai.cybnate.ui.miniapps.MiniAppsScreen
import ai.cybnate.ui.navigation.Screen
import ai.cybnate.ui.navigation.bottomNavItems
import ai.cybnate.ui.terminal.TerminalScreen
import ai.cybnate.ui.theme.CybNateTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Start background terminal/agent foreground service
        startForegroundTerminalService()

        val app = application as CybNateApp

        setContent {
            CybNateTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Chat) }
                var activeMiniApp by remember { mutableStateOf<MiniApp?>(null) }

                val chatViewModel: ChatViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return ChatViewModel(app.database) as T
                        }
                    }
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (activeMiniApp == null) {
                            NavigationBar {
                                bottomNavItems.forEach { item ->
                                    NavigationBarItem(
                                        icon = { Icon(item.icon, contentDescription = item.title) },
                                        label = { Text(item.title) },
                                        selected = currentScreen == item,
                                        onClick = { currentScreen = item }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    val modifier = Modifier.padding(innerPadding)

                    if (activeMiniApp != null) {
                        MiniAppWebViewer(
                            app = activeMiniApp!!,
                            onBack = { activeMiniApp = null },
                            modifier = modifier
                        )
                    } else {
                        when (currentScreen) {
                            Screen.Chat -> ChatScreen(
                                viewModel = chatViewModel,
                                modifier = modifier
                            )

                            Screen.Terminal -> TerminalScreen(
                                sessionManager = app.sessionManager,
                                modifier = modifier
                            )

                            Screen.MiniApps -> MiniAppsScreen(
                                onLaunchApp = { selectedApp ->
                                    activeMiniApp = selectedApp
                                },
                                modifier = modifier
                            )

                            Screen.Settings -> SettingsPlaceholderScreen(modifier = modifier)
                        }
                    }
                }
            }
        }
    }

    private fun startForegroundTerminalService() {
        val intent = Intent(this, TerminalForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}

@Composable
fun SettingsPlaceholderScreen(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize()) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "CybNate Settings",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Engine: Native Kotlin + Compose + Termux PTY")
            Text("Architecture: Target SDK 35 (Android 15 Ready)")
            Text("Agent Backend: Antigravity CLI / MCP Core Bridge")
            Text("Storage: Modern Scoped Storage & Room DB")
        }
    }
}
