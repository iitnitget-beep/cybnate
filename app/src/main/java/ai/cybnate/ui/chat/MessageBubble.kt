package ai.cybnate.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.cybnate.data.model.ChatMessage
import ai.cybnate.data.model.MessageRole
import ai.cybnate.ui.theme.*

@Composable
fun MessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == MessageRole.USER

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (!isUser && message.modelName != null) {
            Text(
                text = message.modelName,
                style = MaterialTheme.typography.labelSmall,
                color = CybNateCyan,
                modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) CybNateCyan else CybNateSurface,
            modifier = Modifier
                .widthIn(max = 350.dp)
                .border(
                    width = 1.dp,
                    color = if (isUser) CybNateCyan else CybNateBorder,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (message.content.contains("```")) {
                    FormattedMarkdownContent(message.content, isUser)
                } else {
                    Text(
                        text = if (message.isStreaming && message.content.isEmpty()) "Processing query..." else message.content,
                        color = if (isUser) Color.Black else Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun FormattedMarkdownContent(content: String, isUser: Boolean) {
    val parts = content.split("```")

    parts.forEachIndexed { index, part ->
        if (index % 2 == 1) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CodeBlockBg)
                    .border(1.dp, CybNateBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = part.trim(),
                    color = CybNateNeonGreen,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        } else {
            if (part.isNotBlank()) {
                Text(
                    text = part,
                    color = if (isUser) Color.Black else Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
