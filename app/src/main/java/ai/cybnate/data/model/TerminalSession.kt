package ai.cybnate.data.model

import java.util.UUID

data class TerminalSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Terminal",
    val workingDir: String,
    val shellPath: String = "/bin/bash",
    val isActive: Boolean = true,
    val pid: Int = -1
)
