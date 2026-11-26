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

    private val ptySessions = mutableMapOf<String, PtyTerminalSession>()

    private val filesDir = context.filesDir
    val homeDir = File(filesDir, "home").apply { mkdirs() }
    val usrDir = File(filesDir, "usr")

    private val defaultEnv: Array<String> by lazy {
        arrayOf(
            "HOME=${homeDir.absolutePath}",
            "PREFIX=${usrDir.absolutePath}",
            "PATH=${usrDir.absolutePath}/bin:${homeDir.absolutePath}/.local/bin:/data/user/0/ai.antigem/files/usr/bin:/system/bin",
            "LD_LIBRARY_PATH=${usrDir.absolutePath}/lib",
            "TMPDIR=${usrDir.absolutePath}/tmp",
            "TERM=xterm-256color"
        )
    }

    init {
        createSession("Main")
    }

    fun createSession(title: String = "Session ${_sessions.value.size + 1}", workDir: String = homeDir.absolutePath): TerminalSession {
        val safeDir = File(workDir).let { if (it.exists()) it.absolutePath else homeDir.absolutePath }
        val newSession = TerminalSession(
            title = title,
            workingDir = safeDir
        )

        val pty = PtyTerminalSession(
            id = newSession.id,
            title = title,
            workDir = safeDir,
            envVars = defaultEnv
        )
        ptySessions[newSession.id] = pty
        try {
            pty.start()
        } catch (_: Exception) {}

        _sessions.value = _sessions.value + newSession
        _activeSessionId.value = newSession.id
        return newSession
    }

    fun getPtySession(id: String): PtyTerminalSession? = ptySessions[id]

    fun switchSession(sessionId: String) {
        if (_sessions.value.any { it.id == sessionId }) {
            _activeSessionId.value = sessionId
        }
    }

    fun closeSession(sessionId: String) {
        ptySessions[sessionId]?.close()
        ptySessions.remove(sessionId)

        val current = _sessions.value
        val remaining = current.filter { it.id != sessionId }
        _sessions.value = remaining

        if (_activeSessionId.value == sessionId) {
            _activeSessionId.value = remaining.lastOrNull()?.id
        }
    }
}
