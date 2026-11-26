package ai.cybnate.data.model

import java.util.UUID

enum class MessageRole {
    USER, MODEL, SYSTEM, TOOL
}

data class ArtifactInfo(
    val title: String,
    val type: String, // code, markdown, html, image
    val content: String,
    val filePath: String? = null
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val modelName: String? = null,
    val artifact: ArtifactInfo? = null,
    val toolCallName: String? = null
)
