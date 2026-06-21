package br.com.tscode.checking

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import br.com.tscode.checking.platform.activitylog.ActivityLogger
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

// Application entry point — Hilt graph root (§6).
// Also initialises WorkManager with the HiltWorkerFactory so @HiltWorker classes work (§23.3-3).
@HiltAndroidApp
class CheckingApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var activityLogger: ActivityLogger

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        activityLogger.logSystem("App started.") // plan004 — process start
    }

    // Two channels for background automatic activities (§23.9):
    // 1. Ongoing service notification (low importance, non-dismissible while running).
    // 2. Activity-performed / auth-expiry events (default importance).
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return

        val serviceChannel = NotificationChannel(
            CHANNEL_ID_SERVICE,
            getString(R.string.notification_channel_service_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            setShowBadge(false)
        }

        val eventsChannel = NotificationChannel(
            CHANNEL_ID_EVENTS,
            getString(R.string.notification_channel_events_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        )

        manager.createNotificationChannels(listOf(serviceChannel, eventsChannel))
    }

    companion object {
        const val CHANNEL_ID_SERVICE = "auto_activities_service"
        const val CHANNEL_ID_EVENTS = "auto_activities_events"
    }
}
