package ai.cybnate.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.cybnate.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showModelMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val availableEngines = listOf(
        "Google Antigravity (AGY)",
        "Claude Code (Anthropic)",
        "OpenCode / Codex",
        "Gemini 2.5 Flash",
        "Local Linux Shell"
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showModelMenu = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = CybNateCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedModel,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
                        }

                        DropdownMenu(
                            expanded = showModelMenu,
                            onDismissRequest = { showModelMenu = false },
                            modifier = Modifier.background(CybNateSurface)
                        ) {
                            availableEngines.forEach { engine ->
                                DropdownMenuItem(
                                    text = { Text(engine, color = Color.White) },
                                    onClick = {
                                        viewModel.setModel(engine)
                                        showModelMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearHistory() }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Clear Chat", tint = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CybNateVoid
                )
            )
        },
        containerColor = CybNateVoid,
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Quick command shortcut chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chips = listOf("!craft-anything-cli doctor", "!agy --version", "!claude --version", "!uname -a", "!free -m")
                items(chips) { chip ->
                    SuggestionChip(
                        onClick = { viewModel.sendMessage(chip) },
                        label = { Text(chip, fontSize = 11.sp, color = CybNateCyan) },
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = CybNateBorder
                        ),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = CybNateSurface
                        )
                    )
                }
            }

            // Message list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    MessageBubble(message = msg)
                }
            }

            // Input Bar
            Surface(
                color = CybNateSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CybNateBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Prompt AI or type !shell_command...", color = Color.Gray, fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = CybNateSurfaceVariant,
                            unfocusedContainerColor = CybNateSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = false,
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !isGenerating) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank() && !isGenerating,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (inputText.isNotBlank() && !isGenerating)
                                    CybNateCyan
                                else CybNateSurfaceVariant
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank() && !isGenerating)
                                Color.Black
                            else Color.Gray
                        )
                    }
                }
            }
        }
    }
}
