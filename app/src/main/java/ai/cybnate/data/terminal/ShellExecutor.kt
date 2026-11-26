package ai.cybnate.data.terminal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

data class CommandResult(
    val exitCode: Int,
    val output: String
)

class ShellExecutor(
    private val defaultWorkDir: String = "/data/user/0/ai.antigem/files/home"
) {
    fun executeStream(command: String, workDir: String = defaultWorkDir): Flow<String> = flow {
        val workingDirectory = File(workDir).let { if (it.exists()) it else File(defaultWorkDir) }

        val processBuilder = ProcessBuilder("sh", "-c", command).apply {
            directory(workingDirectory)
            redirectErrorStream(true)
            environment()["HOME"] = defaultWorkDir
            environment()["PATH"] = "${environment()["PATH"]}:/data/user/0/ai.antigem/files/usr/bin:/data/user/0/ai.antigem/files/home/.local/bin"
        }

        val process = processBuilder.start()
        val reader = BufferedReader(InputStreamReader(process.inputStream))

        try {
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                emit(line + "\n")
            }
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                emit("\n[Process exited with code $exitCode]\n")
            }
        } finally {
            reader.close()
            process.destroy()
        }
    }.flowOn(Dispatchers.IO)

    suspend fun execute(command: String, workDir: String = defaultWorkDir): CommandResult {
        val sb = StringBuilder()
        var exitCode = -1

        val workingDirectory = File(workDir).let { if (it.exists()) it else File(defaultWorkDir) }
        val processBuilder = ProcessBuilder("sh", "-c", command).apply {
            directory(workingDirectory)
            redirectErrorStream(true)
            environment()["HOME"] = defaultWorkDir
            environment()["PATH"] = "${environment()["PATH"]}:/data/user/0/ai.antigem/files/usr/bin:/data/user/0/ai.antigem/files/home/.local/bin"
        }

        val process = processBuilder.start()
        BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append("\n")
            }
        }
        exitCode = process.waitFor()

        return CommandResult(exitCode, sb.toString())
    }
}
