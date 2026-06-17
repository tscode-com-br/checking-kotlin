package br.com.tscode.checking.platform.background.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import br.com.tscode.checking.CheckingApp
import br.com.tscode.checking.MainActivity
import br.com.tscode.checking.R
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.i18n.t

// Central helper for all background automatic-activities notifications (§23.9, T3B.6).
// Three notification types:
//   1. Ongoing service notification — low-importance, non-dismissible (FGS requirement).
//   2. Activity-performed event — check-in/check-out result, auto-cancels.
//   3. Reauth needed — session expired while backgrounded, tapping opens the app.
//
// buildServiceNotification() returns a Notification for startForeground().
// updateServiceNotification() / postActivityNotification() / postReauthNotification() post directly.
object AutoActivityNotifications {

    const val NOTIFICATION_ID_SERVICE = 1001
    const val NOTIFICATION_ID_EVENT = 1002
    const val NOTIFICATION_ID_REAUTH = 1003
    const val NOTIFICATION_ID_PAUSE = 1004
    const val NOTIFICATION_ID_ACCIDENT = 1005

    private const val REQUEST_CODE_TAP = 2000
    private const val REQUEST_CODE_EVENT = 2001
    private const val REQUEST_CODE_REAUTH = 2002
    private const val REQUEST_CODE_PAUSE = 2003
    private const val REQUEST_CODE_ACCIDENT = 2004

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
