package ai.cybnate.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.cybnate.data.agent.AiAgentService
import ai.cybnate.data.db.AppDatabase
import ai.cybnate.data.db.ChatEntity
import ai.cybnate.data.model.ChatMessage
import ai.cybnate.data.model.MessageRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val database: AppDatabase,
    private val agentService: AiAgentService = AiAgentService()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _selectedModel = MutableStateFlow("Gemini 2.5 Flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    init {
        loadPersistedMessages()
    }

    private fun loadPersistedMessages() {
        viewModelScope.launch {
            database.chatDao().getAllMessagesFlow().collectLatest { entities ->
                if (entities.isNotEmpty() || _messages.value.isEmpty()) {
                    _messages.value = entities.map { it.toChatMessage() }
                }
            }
        }
    }

    fun setModel(model: String) {
        _selectedModel.value = model
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _isGenerating.value) return

        val userMessage = ChatMessage(
            role = MessageRole.USER,
            content = text.trim()
        )

        val updatedList = _messages.value + userMessage
        _messages.value = updatedList

        viewModelScope.launch {
            database.chatDao().insertMessage(ChatEntity.fromChatMessage(userMessage))
        }

        // Prepare model streaming placeholder
        val botMessageId = UUID.randomUUID().toString()
        val botMessage = ChatMessage(
            id = botMessageId,
            role = MessageRole.MODEL,
            content = "",
            isStreaming = true,
            modelName = _selectedModel.value
        )
        _messages.value = updatedList + botMessage
        _isGenerating.value = true

        viewModelScope.launch {
            val contentBuilder = StringBuilder()
            try {
                agentService.processPrompt(text, updatedList, _selectedModel.value).collect { chunk ->
                    contentBuilder.append(chunk)
                    _messages.value = _messages.value.map { msg ->
                        if (msg.id == botMessageId) {
                            msg.copy(content = contentBuilder.toString())
                        } else msg
                    }
                }
            } catch (e: Exception) {
                contentBuilder.append("\n\n❌ Error: ${e.message}")
            } finally {
                val finalMessage = botMessage.copy(
                    content = contentBuilder.toString(),
                    isStreaming = false
                )
                _messages.value = _messages.value.map { msg ->
                    if (msg.id == botMessageId) finalMessage else msg
                }
                database.chatDao().insertMessage(ChatEntity.fromChatMessage(finalMessage))
                _isGenerating.value = false
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            database.chatDao().clearHistory()
            _messages.value = emptyList()
        }
    }
}
