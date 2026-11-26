package ai.cybnate.ui.bootstrap

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.cybnate.data.bootstrap.BootstrapManager
import ai.cybnate.data.bootstrap.BootstrapState
import ai.cybnate.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun BootstrapScreen(
    bootstrapManager: BootstrapManager,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var state by remember { mutableStateOf<BootstrapState>(BootstrapState.Idle) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (bootstrapManager.isBootstrapInstalled()) {
            onComplete()
        } else {
            bootstrapManager.installBootstrap().collect { newState ->
                state = newState
                if (newState is BootstrapState.Ready) {
                    onComplete()
                }
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = CybNateVoid
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Cybernetic Core Badge
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(CybNateCyan.copy(alpha = 0.25f), Color.Transparent)
                            )
                        )
                        .border(2.dp, CybNateCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = CybNateCyan,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "CYBNATE",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = Color.White
                )

                Text(
                    text = "Autonomous AI Agent & Linux Workstation",
                    fontSize = 13.sp,
                    color = CybNateCyan,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Progress Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CybNateSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CybNateBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        when (val s = state) {
                            is BootstrapState.Idle -> {
                                Text("Initializing CybNate environment...", color = Color.Gray, fontSize = 13.sp)
                            }

                            is BootstrapState.Downloading -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Downloading Linux RootFS (~300 MB)",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${(s.progress * 100).toInt()}%",
                                        color = CybNateCyan,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                LinearProgressIndicator(
                                    progress = { s.progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = CybNateCyan,
                                    trackColor = CybNateSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = String.format("%.1f MB / %.1f MB (Google Antigravity & Claude Code Runtime)", s.downloadedMb, s.totalMb),
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            is BootstrapState.Extracting -> {
                                Text(
                                    text = "Decompressing RootFS Packages...",
                                    color = CybNateNeonGreen,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = CybNateNeonGreen,
                                    trackColor = CybNateSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = s.currentItem,
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
                            }

                            is BootstrapState.SettingUpBridge -> {
                                Text("Setting up AGY IDE Bridge & Agent Daemon...", color = CybNatePurple, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth().height(4.dp),
                                    color = CybNatePurple
                                )
                            }

                            is BootstrapState.Ready -> {
                                Text("System ready! Launching CybNate...", color = CybNateNeonGreen, fontSize = 14.sp)
                            }

                            is BootstrapState.Error -> {
                                Text(
                                    text = s.message,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        scope.launch {
                                            bootstrapManager.installBootstrap().collect { state = it }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CybNateCyan)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Retry Download", color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
