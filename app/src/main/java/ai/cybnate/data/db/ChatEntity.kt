package ai.cybnate.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import ai.cybnate.data.model.ChatMessage
import ai.cybnate.data.model.MessageRole

@Entity(tableName = "chat_messages")
data class ChatEntity(
    @PrimaryKey val id: String,
    val role: String,
    val content: String,
    val timestamp: Long,
    val modelName: String?,
    val toolCallName: String?
) {
    fun toChatMessage(): ChatMessage {
        val roleEnum = try {
            MessageRole.valueOf(role)
        } catch (_: Exception) {
            MessageRole.MODEL
        }
        return ChatMessage(
            id = id,
            role = roleEnum,
            content = content,
            timestamp = timestamp,
            modelName = modelName,
            toolCallName = toolCallName
        )
    }

    companion object {
        fun fromChatMessage(msg: ChatMessage): ChatEntity {
            return ChatEntity(
                id = msg.id,
                role = msg.role.name,
                content = msg.content,
                timestamp = msg.timestamp,
                modelName = msg.modelName,
                toolCallName = msg.toolCallName
            )
        }
    }
}
