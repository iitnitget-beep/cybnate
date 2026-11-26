package ai.cybnate.data.agent

import android.content.Context
import ai.cybnate.data.bootstrap.BootstrapManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class AgyBridgeManager(private val context: Context) {

    private val bootstrapManager = BootstrapManager(context)
    private var bridgeProcess: Process? = null

    val bridgeUrl = "http://127.0.0.1:1234"
    val hubPort = 1235

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun startBridge(): Boolean = withContext(Dispatchers.IO) {
        val binary = bootstrapManager.bridgeBinary
        if (!binary.exists() || !binary.canExecute()) {
            bootstrapManager.extractBridgeBinary()
        }

        if (isBridgeRunning()) return@withContext true

        val pb = ProcessBuilder(
            binary.absolutePath,
            "-p", "1234",
            "--hub-port", hubPort.toString(),
            "--bind", "127.0.0.1",
            "-f",
            "-d", bootstrapManager.homeDir.absolutePath
        ).apply {
            directory(bootstrapManager.homeDir)
            redirectErrorStream(true)
            val env = environment()
            env["HOME"] = bootstrapManager.homeDir.absolutePath
            env["PREFIX"] = bootstrapManager.usrDir.absolutePath
            env["PATH"] = "${bootstrapManager.binDir.absolutePath}:${bootstrapManager.homeDir.absolutePath}/.local/bin:${env["PATH"]}"
            env["LD_LIBRARY_PATH"] = "${bootstrapManager.usrDir.absolutePath}/lib"
            env["TMPDIR"] = "${bootstrapManager.usrDir.absolutePath}/tmp"
        }

        try {
            File(bootstrapManager.usrDir, "tmp").mkdirs()
            bridgeProcess = pb.start()

            // Poll for bridge ready
            for (i in 1..20) {
                kotlinx.coroutines.delay(250)
                if (isBridgeRunning()) return@withContext true
            }
            isBridgeRunning()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun isBridgeRunning(): Boolean = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url("$bridgeUrl/api/projects").build()
            val resp = httpClient.newCall(req).execute()
            resp.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    fun executeAgentPrompt(
        prompt: String,
        engine: String = "Google Antigravity"
    ): Flow<String> = flow {
        val trimmed = prompt.trim()

        if (!isBridgeRunning()) {
            startBridge()
        }

        try {
            val jsonPayload = JSONObject().apply {
                put("prompt", trimmed)
                put("engine", engine.lowercase())
            }

            val body = jsonPayload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$bridgeUrl/api/agent/stream")
                .post(body)
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.byteStream()?.bufferedReader()?.use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        emit(line + "\n")
                    }
                }
            } else {
                emit("Bridge response error: ${response.code}\nRunning via direct shell fallback...\n")
            }
        } catch (e: Exception) {
            emit("Error connecting to AGY bridge: ${e.message}\nMake sure Linux bootstrap is initialized.")
        }
    }.flowOn(Dispatchers.IO)

    fun stopBridge() {
        bridgeProcess?.destroy()
        bridgeProcess = null
    }
}
