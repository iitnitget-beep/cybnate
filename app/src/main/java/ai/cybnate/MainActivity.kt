package ai.cybnate

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.cybnate.data.agent.AgyBridgeManager
import ai.cybnate.data.bootstrap.BootstrapManager
import ai.cybnate.data.model.MiniApp
import ai.cybnate.service.TerminalForegroundService
import ai.cybnate.ui.bootstrap.BootstrapScreen
import ai.cybnate.ui.chat.ChatScreen
import ai.cybnate.ui.chat.ChatViewModel
import ai.cybnate.ui.miniapps.MiniAppWebViewer
import ai.cybnate.ui.miniapps.MiniAppsScreen
import ai.cybnate.ui.navigation.Screen
import ai.cybnate.ui.navigation.bottomNavItems
import ai.cybnate.ui.terminal.TerminalScreen
import ai.cybnate.ui.theme.CybNateCyan
import ai.cybnate.ui.theme.CybNateTheme
import ai.cybnate.ui.theme.CybNateVoid
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var bootstrapManager: BootstrapManager
    private lateinit var bridgeManager: AgyBridgeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        bootstrapManager = BootstrapManager(applicationContext)
        bridgeManager = AgyBridgeManager(applicationContext)

        startForegroundTerminalService()

        val app = application as CybNateApp

        setContent {
            CybNateTheme {
                var isBootstrapReady by remember {
                    mutableStateOf(bootstrapManager.isBootstrapInstalled())
                }

                if (!isBootstrapReady) {
                    BootstrapScreen(
                        bootstrapManager = bootstrapManager,
                        onComplete = {
                            isBootstrapReady = true
                            lifecycleScope.launch {
                                bridgeManager.startBridge()
                            }
                        }
                    )
                } else {
                    CybNateMainScaffold(
                        app = app,
                        bridgeManager = bridgeManager
                    )
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
fun CybNateMainScaffold(
    app: CybNateApp,
    bridgeManager: AgyBridgeManager
) {
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
                NavigationBar(
                    containerColor = ai.cybnate.ui.theme.CybNateSurface,
                    contentColor = Color.White
                ) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title, fontSize = 12.sp) },
                            selected = currentScreen == item,
                            onClick = { currentScreen = item },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = CybNateCyan,
                                indicatorColor = CybNateCyan,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
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

                Screen.Settings -> CybNateSettingsScreen(
                    bridgeManager = bridgeManager,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
fun CybNateSettingsScreen(
    bridgeManager: AgyBridgeManager,
    modifier: Modifier = Modifier
) {
    var isBridgeActive by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isBridgeActive = bridgeManager.isBridgeRunning()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = CybNateVoid
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = "CYBNATE SYSTEM",
                style = MaterialTheme.typography.titleLarge,
                color = CybNateCyan,
                letterSpacing = 2.sp
            )
            Text(
                text = "Autonomous AI Agent & Linux Workstation (Android 15)",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ai.cybnate.ui.theme.CybNateSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("AGENT & BRIDGE STATUS", fontSize = 13.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isBridgeActive) ai.cybnate.ui.theme.CybNateNeonGreen else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBridgeActive) "AGY Bridge Server Online (127.0.0.1:1234)" else "AGY Bridge Server Ready",
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Google Antigravity (AGY Hub): Connected (Port 1235)", fontSize = 12.sp, color = Color.LightGray)
                    Text("• Claude Code Harness: Enabled (/api/claude)", fontSize = 12.sp, color = Color.LightGray)
                    Text("• Codex & OpenCode: Enabled", fontSize = 12.sp, color = Color.LightGray)
                    Text("• Native Terminal: libtermux.so PTY Subprocess", fontSize = 12.sp, color = Color.LightGray)
                }
            }
        }
    }
}
