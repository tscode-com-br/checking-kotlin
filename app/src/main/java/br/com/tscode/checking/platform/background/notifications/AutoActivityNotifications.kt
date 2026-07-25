package br.com.tscode.checking.platform.background.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import br.com.tscode.checking.CheckingApp
import br.com.tscode.checking.MainActivity
import br.com.tscode.checking.R
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.i18n.t
import java.util.concurrent.atomic.AtomicBoolean

// Central helper for all background automatic-activities notifications (§23.9, T3B.6).
// Background notification types include:
//   1. Ongoing service notification — low-importance, non-dismissible (FGS requirement).
//   2. Activity-performed event — check-in/check-out result, auto-cancels.
//   3. Reauth needed — session expired while backgrounded, tapping opens the app.
//   4. Background-location reminder — a coalesced warning when Android cannot restart the FGS.
//
// buildServiceNotification() returns a Notification for startForeground().
// updateServiceNotification() / postActivityNotification() / postReauthNotification() post directly.
object AutoActivityNotifications {

    const val NOTIFICATION_ID_SERVICE = 1001
    const val NOTIFICATION_ID_EVENT = 1002
    const val NOTIFICATION_ID_REAUTH = 1003
    const val NOTIFICATION_ID_PAUSE = 1004
    const val NOTIFICATION_ID_ACCIDENT = 1005
    const val NOTIFICATION_ID_LOW_ACCURACY = 1006
    const val NOTIFICATION_ID_BACKGROUND_LOCATION = 1007

    private const val REQUEST_CODE_TAP = 2000
    private const val REQUEST_CODE_EVENT = 2001
    private const val REQUEST_CODE_REAUTH = 2002
    private const val REQUEST_CODE_PAUSE = 2003
    private const val REQUEST_CODE_ACCIDENT = 2004
    private const val REQUEST_CODE_LOW_ACCURACY = 2005
    private const val REQUEST_CODE_BACKGROUND_LOCATION = 2006

    private val backgroundLocationWarningPostedThisProcess = AtomicBoolean(false)

    // ─── Service notification ────────────────────────────────────────────────

    fun buildServiceNotification(context: Context, isPaused: Boolean, lang: String): Notification {
        val tapIntent = tapPendingIntent(context, REQUEST_CODE_TAP)
        val body = if (isPaused) {
            t("scheduledPause.notificationPaused", lang = lang)
        } else {
            t("autoActivities.notification.serviceBody", lang = lang)
        }
        return NotificationCompat.Builder(context, CheckingApp.CHANNEL_ID_SERVICE)
            .setContentTitle(t("autoActivities.notification.serviceTitle", lang = lang))
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(tapIntent)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    fun updateServiceNotification(context: Context, isPaused: Boolean, lang: String) {
        notificationManager(context).notify(
            NOTIFICATION_ID_SERVICE,
            buildServiceNotification(context, isPaused, lang),
        )
    }

    // ─── Activity-performed notification ────────────────────────────────────

    fun postActivityNotification(
        context: Context,
        action: CheckAction,
        local: String?,
        lang: String,
    ) {
        val message = if (action == CheckAction.CHECKIN) {
            t("autoActivities.notification.checkinMessage", lang = lang)
        } else {
            t("autoActivities.notification.checkoutMessage", lang = lang)
        }
        postSimpleEvent(context, NOTIFICATION_ID_EVENT, REQUEST_CODE_EVENT, message, lang)
    }

    // ─── Scheduled-pause transition notification ─────────────────────────────

    // started=true → "Checking em pausa."; started=false → "Checking em atividade."
    fun postScheduledPauseTransition(context: Context, started: Boolean, lang: String) {
        val message = if (started) {
            t("autoActivities.notification.pauseStartMessage", lang = lang)
        } else {
            t("autoActivities.notification.pauseEndMessage", lang = lang)
        }
        postSimpleEvent(context, NOTIFICATION_ID_PAUSE, REQUEST_CODE_PAUSE, message, lang)
    }

    // ─── Accident notification ───────────────────────────────────────────────

    fun postAccidentNotification(context: Context, lang: String) {
        val message = t("autoActivities.notification.accidentMessage", lang = lang)
        postSimpleEvent(context, NOTIFICATION_ID_ACCIDENT, REQUEST_CODE_ACCIDENT, message, lang)
    }

    // ─── Low-accuracy retry episode notification ────────────────────────────

    // Posted once when an accuracy-retry episode starts. A dedicated, stable notification id
    // coalesces the message even if Android restores/reposts it around process lifecycle events.
    fun postLowAccuracyRetryNotification(
        context: Context,
        expectedAction: CheckAction?,
        lang: String,
    ) {
        val titleKey = when (expectedAction) {
            CheckAction.CHECKIN -> "lowAccuracyRetry.checkinTitle"
            CheckAction.CHECKOUT -> "lowAccuracyRetry.checkoutTitle"
            null -> "lowAccuracyRetry.automaticActivityTitle"
        }
        val notification = NotificationCompat.Builder(context, CheckingApp.CHANNEL_ID_EVENTS)
            .setContentTitle(t(titleKey, lang = lang))
            .setContentText(t("lowAccuracyRetry.body", lang = lang))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(tapPendingIntent(context, REQUEST_CODE_LOW_ACCURACY))
            .setAutoCancel(true)
            .build()
        notificationManager(context).notify(NOTIFICATION_ID_LOW_ACCURACY, notification)
    }

    fun cancelLowAccuracyRetryNotification(context: Context) {
        notificationManager(context).cancel(NOTIFICATION_ID_LOW_ACCURACY)
    }

    // ─── Background-location restart warning ─────────────────────────────────

    /**
     * Warns that the automatic engine lacks reliable background-location access.
     *
     * A stable id coalesces boot/geofence/watchdog attempts. The process guard prevents a dismissed
     * notification from being recreated every 15 minutes; a new process/restart may remind once
     * again. Posting is best-effort because the user may also have revoked POST_NOTIFICATIONS.
     */
    fun postBackgroundLocationRequiredNotification(
        context: Context,
        lang: String,
    ) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        if (!backgroundLocationWarningPostedThisProcess.compareAndSet(false, true)) return

        val body = t("backgroundLocationRestart.body", lang = lang)
        val notification =
            NotificationCompat.Builder(context, CheckingApp.CHANNEL_ID_EVENTS)
                .setContentTitle(t("backgroundLocationRestart.title", lang = lang))
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(tapPendingIntent(context, REQUEST_CODE_BACKGROUND_LOCATION))
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .build()
        val posted =
            runCatching {
                notificationManager(context).notify(
                    NOTIFICATION_ID_BACKGROUND_LOCATION,
                    notification,
                )
            }.isSuccess
        if (!posted) backgroundLocationWarningPostedThisProcess.set(false)
    }

    fun cancelBackgroundLocationRequiredNotification(context: Context) {
        backgroundLocationWarningPostedThisProcess.set(false)
        runCatching {
            notificationManager(context).cancel(NOTIFICATION_ID_BACKGROUND_LOCATION)
        }
    }

    // Shared builder for the simple "brand title + message" event notifications.
    private fun postSimpleEvent(
        context: Context,
        notificationId: Int,
        requestCode: Int,
        message: String,
        lang: String,
    ) {
        val notification = NotificationCompat.Builder(context, CheckingApp.CHANNEL_ID_EVENTS)
            .setContentTitle(t("autoActivities.notification.brandTitle", lang = lang))
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(tapPendingIntent(context, requestCode))
            .setAutoCancel(true)
            .build()
        notificationManager(context).notify(notificationId, notification)
    }

    // ─── Reauth notification ─────────────────────────────────────────────────

    // Posted when the background session expires and silent re-login (T3B.7) fails or
    // is not attempted. Tapping brings the user back to the login screen.
    fun postReauthNotification(context: Context, lang: String) {
        val notification = NotificationCompat.Builder(context, CheckingApp.CHANNEL_ID_EVENTS)
            .setContentTitle(t("autoActivities.notification.reauthTitle", lang = lang))
            .setContentText(t("autoActivities.notification.reauthBody", lang = lang))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(tapPendingIntent(context, REQUEST_CODE_REAUTH))
            .setAutoCancel(true)
            .build()
        notificationManager(context).notify(NOTIFICATION_ID_REAUTH, notification)
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun tapPendingIntent(context: Context, requestCode: Int): PendingIntent =
        PendingIntent.getActivity(
            context, requestCode,
            Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun notificationManager(context: Context): NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}
