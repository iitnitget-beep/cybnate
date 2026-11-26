package ai.cybnate

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import ai.cybnate.data.db.AppDatabase
import ai.cybnate.data.terminal.TerminalSessionManager

class CybNateApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var sessionManager: TerminalSessionManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getDatabase(this)
        sessionManager = TerminalSessionManager(this)

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_TERMINAL_SERVICE,
                "Terminal & Agent Background Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps terminal sessions and agent operations running in background"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_TERMINAL_SERVICE = "cybnate_terminal_service"

        lateinit var instance: CybNateApp
            private set
    }
}
