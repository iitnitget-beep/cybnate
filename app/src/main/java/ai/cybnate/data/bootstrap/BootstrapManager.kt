package ai.cybnate.data.bootstrap

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import org.tukaani.xz.XZInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

sealed class BootstrapState {
    object Idle : BootstrapState()
    data class Downloading(val progress: Float, val downloadedMb: Float, val totalMb: Float) : BootstrapState()
    data class Extracting(val currentItem: String) : BootstrapState()
    object SettingUpBridge : BootstrapState()
    object Ready : BootstrapState()
    data class Error(val message: String) : BootstrapState()
}

class BootstrapManager(private val context: Context) {

    private val filesDir = context.filesDir
    val usrDir = File(filesDir, "usr")
    val homeDir = File(filesDir, "home")
    val binDir = File(usrDir, "bin")
    val bridgeBinary = File(binDir, "agy_ide_bridge")

    private val client = OkHttpClient()

    private val primaryBootstrapUrl =
        "https://github.com/Santoshkurmi/antigem/releases/download/rootfs-v2/bootrapz_2026-11-19.zip"
    private val fallbackBootstrapUrl =
        "https://huggingface.co/datasets/santoshkurmi/rootfs-v2/resolve/main/bootrapz_2026-11-19.zip"

    fun isBootstrapInstalled(): Boolean {
        val bash = File(binDir, "bash")
        return bash.exists() && bash.canExecute() && bridgeBinary.exists()
    }

    fun installBootstrap(): Flow<BootstrapState> = flow {
        try {
            emit(BootstrapState.Downloading(0f, 0f, 310f))

            val tempZip = File(context.cacheDir, "bootstrap_download.zip")

            // 1. Download RootFS Archive
            val downloaded = downloadFileWithProgress(primaryBootstrapUrl, tempZip) { progress, downMb, totalMb ->
                emit(BootstrapState.Downloading(progress, downMb, totalMb))
            } || downloadFileWithProgress(fallbackBootstrapUrl, tempZip) { progress, downMb, totalMb ->
                emit(BootstrapState.Downloading(progress, downMb, totalMb))
            }

            if (!downloaded || !tempZip.exists() || tempZip.length() == 0L) {
                emit(BootstrapState.Error("Failed to download Linux rootfs from GitHub & HuggingFace mirrors"))
                return@flow
            }

            // 2. Extract Archive into filesDir
            usrDir.mkdirs()
            homeDir.mkdirs()
            emit(BootstrapState.Extracting("Extracting Linux runtime files..."))

            unzipArchive(tempZip) { entryName ->
                emit(BootstrapState.Extracting(entryName))
            }
            tempZip.delete()

            // 3. Set up agy_ide_bridge and helper binaries from assets
            emit(BootstrapState.SettingUpBridge)
            extractBridgeBinary()
            extractAssetHelpers()

            // 4. Set executable permissions on all binaries
            setExecutablePermissions(binDir)

            emit(BootstrapState.Ready)
        } catch (e: Exception) {
            emit(BootstrapState.Error("Bootstrap installation failed: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun downloadFileWithProgress(
        url: String,
        dest: File,
        onProgress: suspend (progress: Float, downloadedMb: Float, totalMb: Float) -> Unit
    ): Boolean {
        return try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return false

            val body = response.body ?: return false
            val contentLength = body.contentLength().toFloat()
            val totalMb = if (contentLength > 0) contentLength / (1024 * 1024) else 310f

            body.byteStream().use { input ->
                FileOutputStream(dest).use { output ->
                    val buffer = ByteArray(32 * 1024)
                    var read: Int
                    var totalRead = 0L

                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        totalRead += read
                        val downMb = totalRead.toFloat() / (1024 * 1024)
                        val progress = if (contentLength > 0) totalRead / contentLength else (downMb / totalMb)
                        onProgress(progress.coerceIn(0f, 1f), downMb, totalMb)
                    }
                    output.flush()
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun unzipArchive(zipFile: File, onEntry: (String) -> Unit) {
        ZipInputStream(zipFile.inputStream().buffered()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val file = File(filesDir, entry.name)
                onEntry(entry.name)
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    FileOutputStream(file).use { fos ->
                        zis.copyTo(fos)
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    fun extractBridgeBinary() {
        binDir.mkdirs()
        try {
            context.assets.open("bin/agy_ide_bridge.xz").use { input ->
                XZInputStream(input).use { xzIn ->
                    FileOutputStream(bridgeBinary).use { out ->
                        xzIn.copyTo(out)
                    }
                }
            }
            bridgeBinary.setExecutable(true, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun extractAssetHelpers() {
        val assetBinaries = listOf(
            "am.apk", "backup_bootstrap", "backup_chats", "backup_projects",
            "git_clone", "restore_chats", "restore_projects"
        )
        for (name in assetBinaries) {
            try {
                val dest = File(binDir, name)
                context.assets.open("bin/$name").use { input ->
                    FileOutputStream(dest).use { out ->
                        input.copyTo(out)
                    }
                }
                dest.setExecutable(true, false)
            } catch (_: Exception) {}
        }
    }

    private fun setExecutablePermissions(dir: File) {
        dir.listFiles()?.forEach { file ->
            if (file.isFile) {
                file.setExecutable(true, false)
            }
        }
    }
}
