package ai.cybnate.data.terminal

import android.content.Context
import ai.cybnate.data.model.TerminalSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class TerminalSessionManager(private val context: Context) {

    private val _sessions = MutableStateFlow<List<TerminalSession>>(emptyList())
    val sessions: StateFlow<List<TerminalSession>> = _sessions.asStateFlow()

    private val _activeSessionId = MutableStateFlow<String?>(null)
    val activeSessionId: StateFlow<String?> = _activeSessionId.asStateFlow()

    private val defaultHome = context.filesDir.parentFile?.absolutePath?.plus("/home")
        ?: "/data/user/0/ai.antigem/files/home"

    init {
        // Create initial default terminal session
        createSession("Main")
    }

    fun createSession(title: String = "Session ${_sessions.value.size + 1}", workDir: String = defaultHome): TerminalSession {
        val safeDir = File(workDir).let { if (it.exists()) it.absolutePath else defaultHome }
        val newSession = TerminalSession(
            title = title,
            workingDir = safeDir
        )
        _sessions.value = _sessions.value + newSession
        _activeSessionId.value = newSession.id
        return newSession
    }

    fun switchSession(sessionId: String) {
        if (_sessions.value.any { it.id == sessionId }) {
            _activeSessionId.value = sessionId
        }
    }

    fun closeSession(sessionId: String) {
        val current = _sessions.value
        val remaining = current.filter { it.id != sessionId }
        _sessions.value = remaining

        if (_activeSessionId.value == sessionId) {
            _activeSessionId.value = remaining.lastOrNull()?.id
        }
    }
}
