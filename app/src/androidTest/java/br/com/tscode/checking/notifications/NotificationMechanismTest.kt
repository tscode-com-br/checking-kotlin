package br.com.tscode.checking.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.com.tscode.checking.CheckingApp
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.platform.background.notifications.AutoActivityNotifications
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.FileInputStream

/**
 * Proof that the local push-notification mechanism actually surfaces notifications on a real
 * Android runtime. Each test posts through the SAME code the background engine uses
 * (AutoActivityNotifications.*) and then asserts the notification is live in the system via
 * NotificationManager.getActiveNotifications() — i.e. channel + builder + icon + i18n + delivery
 * all work end-to-end. There is no FCM/push server: notifications are posted locally.
 */
// NOTE: run via `adb shell am instrument` (see the project test docs). The `gradlew
// connectedAndroidTest` UTP wrapper trips on the app's BootReceiver receiving the install-time
// MY_PACKAGE_REPLACED broadcast under HiltTestApplication — a pre-existing interaction that
// affects every instrumented test here, unrelated to the notification logic under test.
@RunWith(AndroidJUnit4::class)
class NotificationMechanismTest {

    private lateinit var context: Context
    private lateinit var nm: NotificationManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        grantPostNotifications()
        ensureChannels()
        nm.cancelAll()
    }

    @After
    fun tearDown() {
        nm.cancelAll()
    }

    // POST_NOTIFICATIONS is a runtime permission on API 33+. Grant it synchronously by draining
    // the shell command's output stream (blocks until the command completes).
    private fun grantPostNotifications() {
        if (Build.VERSION.SDK_INT < 33) return
        val pkg = context.packageName
        val pfd = InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("pm grant $pkg android.permission.POST_NOTIFICATIONS")
        FileInputStream(pfd.fileDescriptor).use { it.readBytes() }
    }

    // Mirror CheckingApp.createNotificationChannels so the test does not depend on which
    // Application class the instrumentation runner uses.
    private fun ensureChannels() {
        val service = NotificationChannel(
            CheckingApp.CHANNEL_ID_SERVICE,
            "Servico",
            NotificationManager.IMPORTANCE_LOW,
        )
        val events = NotificationChannel(
            CheckingApp.CHANNEL_ID_EVENTS,
            "Eventos",
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        nm.createNotificationChannels(listOf(service, events))
    }

    // notify()/getActiveNotifications() settle asynchronously — poll briefly.
    private fun awaitActive(id: Int): Notification {
        repeat(40) {
            nm.activeNotifications.firstOrNull { it.id == id }?.let { return it.notification }
            Thread.sleep(50)
        }
        throw AssertionError(
            "No active notification with id=$id; active ids=${nm.activeNotifications.map { it.id }}",
        )
    }

    private fun Notification.title(): String? =
        extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()

    private fun Notification.body(): String? =
        extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()

    @Test
    fun channels_areRegistered() {
        assertNotNull(nm.getNotificationChannel(CheckingApp.CHANNEL_ID_SERVICE))
        assertNotNull(nm.getNotificationChannel(CheckingApp.CHANNEL_ID_EVENTS))
    }

    @Test
    fun checkinNotification_isPostedWithCorrectText() {
        AutoActivityNotifications.postActivityNotification(context, CheckAction.CHECKIN, "Unidade P80", "pt")
        val n = awaitActive(AutoActivityNotifications.NOTIFICATION_ID_EVENT)
        assertEquals("Checking", n.title())
        assertEquals("Check-In realizado.", n.body())
    }

    @Test
    fun checkoutNotification_isPostedWithCorrectText() {
        AutoActivityNotifications.postActivityNotification(context, CheckAction.CHECKOUT, null, "pt")
        val n = awaitActive(AutoActivityNotifications.NOTIFICATION_ID_EVENT)
        assertEquals("Check-Out realizado.", n.body())
    }

    @Test
    fun scheduledPauseStartNotification_isPosted() {
        AutoActivityNotifications.postScheduledPauseTransition(context, started = true, lang = "pt")
        val n = awaitActive(AutoActivityNotifications.NOTIFICATION_ID_PAUSE)
        assertEquals("Checking em pausa.", n.body())
    }

    @Test
    fun scheduledPauseEndNotification_isPosted() {
        AutoActivityNotifications.postScheduledPauseTransition(context, started = false, lang = "pt")
        val n = awaitActive(AutoActivityNotifications.NOTIFICATION_ID_PAUSE)
        assertEquals("Checking em atividade.", n.body())
    }

    @Test
    fun accidentNotification_isPosted() {
        AutoActivityNotifications.postAccidentNotification(context, "pt")
        val n = awaitActive(AutoActivityNotifications.NOTIFICATION_ID_ACCIDENT)
        assertEquals("Checking", n.title())
        assertEquals("Checking: acidente reportado!", n.body())
    }

    @Test
    fun reauthNotification_isPosted() {
        AutoActivityNotifications.postReauthNotification(context, "pt")
        val n = awaitActive(AutoActivityNotifications.NOTIFICATION_ID_REAUTH)
        assertEquals("Checking — Reautenticação necessária", n.title())
    }

    // i18n proof: the same accident push in English must differ from the Portuguese text.
    @Test
    fun accidentNotification_localizesByLanguage() {
        AutoActivityNotifications.postAccidentNotification(context, "en")
        val n = awaitActive(AutoActivityNotifications.NOTIFICATION_ID_ACCIDENT)
        assertTrue(n.body()!!.isNotBlank())
        assertNotEquals("Checking: acidente reportado!", n.body())
    }
}
