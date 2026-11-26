package ai.cybnate.data.terminal

import com.termux.terminal.JNI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.reflect.Constructor

class PtyTerminalSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "Terminal",
    val workDir: String,
    val envVars: Array<String>
) {
    private var ptyFd: Int = -1
    private var processId: Int = -1
    private var inputStream: FileInputStream? = null
    private var outputStream: FileOutputStream? = null

    private val _outputFlow = MutableSharedFlow<String>(replay = 100)
    val outputFlow: SharedFlow<String> = _outputFlow.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    fun start(rows: Int = 24, cols: Int = 80) {
        val pidArray = IntArray(1)
        val shell = if (File("$workDir/../usr/bin/bash").exists()) "$workDir/../usr/bin/bash" else "/bin/sh"

        ptyFd = JNI.createSubprocess(
            shell,
            workDir,
            arrayOf(shell, "-l"),
            envVars,
            pidArray,
            rows,
            cols
        )
        processId = pidArray[0]

        if (ptyFd > 0) {
            JNI.setPtyUTF8Mode(ptyFd)
            val fdObj = createFileDescriptor(ptyFd)
            inputStream = FileInputStream(fdObj)
            outputStream = FileOutputStream(fdObj)

            scope.launch {
                readPtyLoop()
            }
        }
    }

    private suspend fun readPtyLoop() {
        val buffer = ByteArray(4096)
        try {
            while (ptyFd > 0) {
                val read = inputStream?.read(buffer) ?: -1
                if (read <= 0) break
                val str = String(buffer, 0, read)
                _outputFlow.emit(str)
            }
        } catch (_: Exception) {
        } finally {
            close()
        }
    }

    fun write(input: String) {
        try {
            outputStream?.write(input.toByteArray())
            outputStream?.flush()
        } catch (_: Exception) {}
    }

    fun resize(rows: Int, cols: Int) {
        if (ptyFd > 0) {
            JNI.setPtyWindowSize(ptyFd, rows, cols)
        }
    }

    fun close() {
        if (ptyFd > 0) {
            try {
                inputStream?.close()
                outputStream?.close()
                JNI.close(ptyFd)
            } catch (_: Exception) {}
            ptyFd = -1
        }
    }

    private fun createFileDescriptor(fd: Int): FileDescriptor {
        val descriptor = FileDescriptor()
        try {
            val field = FileDescriptor::class.java.getDeclaredField("descriptor")
            field.isAccessible = true
            field.setInt(descriptor, fd)
        } catch (_: Exception) {
            try {
                val field = FileDescriptor::class.java.getDeclaredField("fd")
                field.isAccessible = true
                field.setInt(descriptor, fd)
            } catch (_: Exception) {}
        }
        return descriptor
    }
}
