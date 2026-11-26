package ai.cybnate.ui.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.cybnate.data.terminal.TerminalSessionManager
import ai.cybnate.ui.theme.CybNateCyan
import ai.cybnate.ui.theme.TerminalBackground
import ai.cybnate.ui.theme.TerminalGreen
import ai.cybnate.ui.theme.TerminalSurface
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    sessionManager: TerminalSessionManager,
    modifier: Modifier = Modifier
) {
    val sessions by sessionManager.sessions.collectAsState()
    val activeId by sessionManager.activeSessionId.collectAsState()

    var outputBuffer by remember { mutableStateOf("CybNate PTY Terminal [libtermux.so]\n$ ") }
    var currentInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Listen to active PTY session output
    LaunchedEffect(activeId) {
        activeId?.let { id ->
            val pty = sessionManager.getPtySession(id)
            pty?.outputFlow?.collectLatest { chunk ->
                outputBuffer += chunk
                if (outputBuffer.lines().size > 1000) {
                    outputBuffer = outputBuffer.lines().takeLast(800).joinToString("\n")
                }
            }
        }
    }

    LaunchedEffect(outputBuffer.length) {
        if (outputBuffer.isNotEmpty()) {
            listState.animateScrollToItem(outputBuffer.lines().size.coerceAtLeast(1) - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBackground)
    ) {
        // Multi-tab bar
        ScrollableTabRow(
            selectedTabIndex = sessions.indexOfFirst { it.id == activeId }.coerceAtLeast(0),
            containerColor = TerminalSurface,
            contentColor = Color.White,
            edgePadding = 8.dp,
            divider = {}
        ) {
            sessions.forEach { session ->
                Tab(
                    selected = session.id == activeId,
                    onClick = { sessionManager.switchSession(session.id) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(session.title, fontSize = 12.sp, color = if (session.id == activeId) CybNateCyan else Color.Gray)
                            if (sessions.size > 1) {
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { sessionManager.closeSession(session.id) },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close Tab", modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                )
            }
            IconButton(onClick = { sessionManager.createSession() }) {
                Icon(Icons.Default.Add, contentDescription = "New Tab", tint = CybNateCyan)
            }
        }

        // Terminal Output Screen
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(outputBuffer.lines()) { line ->
                    Text(
                        text = line,
                        color = TerminalGreen,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Virtual Keyboard Quick Bar
        VirtualKeyboardRow(
            onKeyPress = { key ->
                activeId?.let { id ->
                    val pty = sessionManager.getPtySession(id)
                    when (key) {
                        "ESC" -> pty?.write("\u001b")
                        "TAB" -> pty?.write("\t")
                        "CTRL" -> {}
                        "↑" -> pty?.write("\u001b[A")
                        "↓" -> pty?.write("\u001b[B")
                        "→" -> pty?.write("\u001b[C")
                        "←" -> pty?.write("\u001b[D")
                        else -> {
                            currentInput += key
                        }
                    }
                }
            }
        )

        // Terminal Command Input
        Surface(
            color = TerminalSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$ ",
                    color = CybNateCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )

                TextField(
                    value = currentInput,
                    onValueChange = { currentInput = it },
                    placeholder = { Text("type command or press Run...", color = Color.Gray, fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )

                Button(
                    onClick = {
                        val cmd = currentInput.trim()
                        if (cmd.isNotEmpty()) {
                            activeId?.let { id ->
                                val pty = sessionManager.getPtySession(id)
                                pty?.write("$cmd\n")
                            }
                            currentInput = ""
                        }
                    },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CybNateCyan),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Run", color = Color.Black, fontSize = 12.sp)
                }
            }
        }
    }
}
