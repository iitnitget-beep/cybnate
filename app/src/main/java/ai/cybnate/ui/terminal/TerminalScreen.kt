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
import ai.cybnate.data.terminal.ShellExecutor
import ai.cybnate.data.terminal.TerminalSessionManager
import ai.cybnate.ui.theme.TerminalBackground
import ai.cybnate.ui.theme.TerminalGreen
import ai.cybnate.ui.theme.TerminalSurface
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    sessionManager: TerminalSessionManager,
    modifier: Modifier = Modifier
) {
    val sessions by sessionManager.sessions.collectAsState()
    val activeId by sessionManager.activeSessionId.collectAsState()

    var outputBuffer by remember { mutableStateOf("Welcome to CybNate Terminal\nType commands or use craft-anything-cli\n$ ") }
    var currentInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val shellExecutor = remember { ShellExecutor() }

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
                            Text(session.title, fontSize = 13.sp)
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
                Icon(Icons.Default.Add, contentDescription = "New Tab", tint = MaterialTheme.colorScheme.primary)
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
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Virtual Keyboard Quick Bar
        VirtualKeyboardRow(
            onKeyPress = { key ->
                currentInput += when (key) {
                    "TAB" -> "\t"
                    "ESC" -> ""
                    else -> key
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
                    color = TerminalGreen,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )

                TextField(
                    value = currentInput,
                    onValueChange = { currentInput = it },
                    placeholder = { Text("type command...", color = Color.Gray, fontSize = 13.sp) },
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
                            outputBuffer += "$cmd\n"
                            currentInput = ""

                            scope.launch {
                                shellExecutor.executeStream(cmd).collect { chunk ->
                                    outputBuffer += chunk
                                }
                                outputBuffer += "$ "
                            }
                        }
                    },
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Run", fontSize = 12.sp)
                }
            }
        }
    }
}
