package ai.cybnate.ui.miniapps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.cybnate.data.model.MiniApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniAppsScreen(
    onLaunchApp: (MiniApp) -> Unit,
    modifier: Modifier = Modifier
) {
    val sampleApps = remember {
        listOf(
            MiniApp(
                id = "system_monitor",
                name = "System Monitor",
                description = "Realtime CPU, RAM and disk usage visualizer",
                iconEmoji = "📊",
                entryHtmlPath = "miniapps/monitor.html"
            ),
            MiniApp(
                id = "apk_analyzer",
                name = "APK Inspector",
                description = "Quickly triage Android APK packages and manifests",
                iconEmoji = "📦",
                entryHtmlPath = "miniapps/apk_analyzer.html"
            ),
            MiniApp(
                id = "code_editor",
                name = "Nano Web IDE",
                description = "Lightweight code editor for scripts and configs",
                iconEmoji = "📝",
                entryHtmlPath = "miniapps/editor.html"
            ),
            MiniApp(
                id = "network_tools",
                name = "Net Diagnostics",
                description = "Ping, traceroute and network packet inspect",
                iconEmoji = "🌐",
                entryHtmlPath = "miniapps/network.html"
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mini-Apps Hub", style = MaterialTheme.typography.titleMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(sampleApps, key = { it.id }) { app ->
                MiniAppCard(app = app, onClick = { onLaunchApp(app) })
            }
        }
    }
}

@Composable
fun MiniAppCard(
    app: MiniApp,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(app.iconEmoji, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = app.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = app.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 2
            )
        }
    }
}
