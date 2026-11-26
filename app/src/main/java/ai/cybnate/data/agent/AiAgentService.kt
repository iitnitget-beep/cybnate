package ai.cybnate.data.agent

import ai.cybnate.data.model.ChatMessage
import ai.cybnate.data.model.MessageRole
import ai.cybnate.data.terminal.ShellExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject

class AiAgentService(
    private val shellExecutor: ShellExecutor = ShellExecutor()
) {

    /**
     * Executes prompt through the agent pipeline.
     * Supports direct command evaluation if prompt starts with `!` or `$` or `/sh`.
     */
    fun processPrompt(
        prompt: String,
        history: List<ChatMessage> = emptyList(),
        model: String = "Gemini 2.5 Flash"
    ): Flow<String> = flow {
        val trimmed = prompt.trim()

        if (trimmed.startsWith("!") || trimmed.startsWith("$") || trimmed.startsWith("/sh ")) {
            val cmd = when {
                trimmed.startsWith("/sh ") -> trimmed.removePrefix("/sh ")
                else -> trimmed.substring(1).trim()
            }
            emit("⚙️ Executing shell command: `$cmd`\n\n```sh\n")
            shellExecutor.executeStream(cmd).collect { chunk ->
                emit(chunk)
            }
            emit("```\n")
            return@flow
        }

        // Agent Assistant Streaming Mock / Local Protocol
        emit("Analyzing request with **$model**...\n\n")

        // In production, OkHttp SSE or Google GenAI SDK handles live websocket/REST streaming
        val response = generateAssistantResponse(trimmed)
        for (token in response.split(" ")) {
            emit("$token ")
            kotlinx.coroutines.delay(18)
        }
    }.flowOn(Dispatchers.IO)

    private fun generateAssistantResponse(query: String): String {
        return when {
            query.contains("status", ignoreCase = true) ->
                "CybNate engine is active. Terminal, MCP agent subsystem, and Compose UI are ready."
            query.contains("help", ignoreCase = true) ->
                "You can chat with me, run shell commands directly using `!command`, inspect Android packages with craft-anything, or launch mini-apps from the Mini-Apps tab."
            else ->
                "I have processed your query: \"$query\". I can execute bash commands, run code analysis, or automate mobile workflows directly on this device."
        }
    }
}
