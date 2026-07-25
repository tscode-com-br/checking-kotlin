package br.com.tscode.checking.platform.background

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.PowerManager
import androidx.datastore.preferences.core.Preferences
import br.com.tscode.checking.core.error.ApiError
import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.core.time.Clock
import br.com.tscode.checking.data.local.AppPreferencesDataSource
import br.com.tscode.checking.data.local.SecurePasswordStore
import br.com.tscode.checking.domain.clientstate.UserSettings
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.HistoryState
import br.com.tscode.checking.domain.model.LocationOptions
import br.com.tscode.checking.domain.repository.AccidentRepository
import br.com.tscode.checking.domain.repository.AuthRepository
import br.com.tscode.checking.domain.repository.CheckRepository
import br.com.tscode.checking.domain.usecase.AutoActivitiesResult
import br.com.tscode.checking.domain.usecase.RunAutomaticActivitiesUseCase
import br.com.tscode.checking.platform.background.notifications.AutoActivityNotifications
import br.com.tscode.checking.platform.location.LocationProvider
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduledPauseDeferralOrchestratorTest {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    private val chave = MutableStateFlow("HR70")
    private val language = MutableStateFlow("pt")
    private val settingsJson = MutableStateFlow("")
    private val runtimeJson = MutableStateFlow("")
    private val pauseFlag = MutableStateFlow(false)
    private val runtimeWrites = mutableListOf<String>()
    private val storedPreferences = mockk<Preferences>(relaxed = true)
    private val appPrefs = mockk<AppPreferencesDataSource>(relaxed = true)
    private val checkRepository = mockk<CheckRepository>()
    private val useCase = mockk<RunAutomaticActivitiesUseCase>()
    private val clock = MutableClock(instantAtLocal(12, 0, 0))
    private var remoteResult: AppResult<HistoryState> = AppResult.Success(history(CheckAction.CHECKOUT))

    @Before
    fun setUp() {
        every { appPrefs.chave } returns chave
        every { appPrefs.language } returns language
        every { appPrefs.userSettingsJson } returns settingsJson
        every { appPrefs.scheduledPauseRuntimeJson } returns runtimeJson
        every { appPrefs.getFlag(any()) } returns pauseFlag
        coEvery { appPrefs.setScheduledPauseRuntimeJson(any()) } answers {
            val value = firstArg<String>()
            runtimeWrites += value
            runtimeJson.value = value
            storedPreferences
        }
        coEvery { appPrefs.setFlag(any(), any()) } answers {
            pauseFlag.value = secondArg()
            storedPreferences
        }

        coEvery { checkRepository.getLocations() } returns
            AppResult.Success(LocationOptions(emptyList(), 30, 15))
        coEvery { checkRepository.getState("HR70") } answers { remoteResult }
        coEvery { useCase(any(), any(), any(), any(), any()) } returns AutoActivitiesResult.NoAction

        mockkObject(AutoActivityNotifications)
        every {
            AutoActivityNotifications.postScheduledPauseTransition(any(), any(), any())
        } just Runs
        every {
            AutoActivityNotifications.cancelLowAccuracyRetryNotification(any())
        } just Runs
        every {
            AutoActivityNotifications.postLowAccuracyRetryNotification(any(), any(), any())
        } just Runs

        mockkStatic(PendingIntent::class)
        every {
            PendingIntent.getService(any(), any(), any(), any())
        } returns mockk(relaxed = true)

        setSettings()
    }

    @After
    fun tearDown() {
        unmockkStatic(PendingIntent::class)
        unmockkObject(AutoActivityNotifications)
    }

    @Test
    fun `checkout at boundary starts active pause immediately`() = runTest {
        remoteResult = AppResult.Success(
            history(CheckAction.CHECKOUT, instantAtLocal(0, 0, 0).minusSeconds(60)),
        )
        val orchestrator = orchestrator(this)

        orchestrator.runOnce(OrchestratorTrigger.PAUSE_START)

        assertEquals(ScheduledPauseRuntimePhase.ACTIVE, runtime().phase)
        coVerify(exactly = 0) { useCase(any(), any(), any(), any(), any()) }
        verify(exactly = 1) {
            AutoActivityNotifications.postScheduledPauseTransition(any(), true, "pt")
        }
        cleanup(orchestrator)
    }

    @Test
    fun `delayed boundary honors checkout timestamp inside occurrence`() = runTest {
        clock.instant = instantAtLocal(12, 0, 5)
        setSettings(from = "12:00", to = "13:00")
        val checkoutAt = instantAtLocal(12, 0, 2)
        remoteResult = AppResult.Success(history(CheckAction.CHECKOUT, checkoutAt))
        val orchestrator = orchestrator(this)

        orchestrator.runOnce(OrchestratorTrigger.PAUSE_START)

        val runtime = runtime()
        assertEquals(ScheduledPauseRuntimePhase.GRACE, runtime.phase)
        assertEquals(checkoutAt.plusSeconds(10).toEpochMilli(), runtime.activateAtEpochMs)
        verify(exactly = 0) {
            AutoActivityNotifications.postScheduledPauseTransition(any(), true, any())
        }
        cleanup(orchestrator)
    }

    @Test
    fun `no history at boundary starts active pause immediately`() = runTest {
        remoteResult = AppResult.Success(history(null, null))
        val orchestrator = orchestrator(this)

        orchestrator.runOnce(OrchestratorTrigger.PAUSE_START)

        assertEquals(ScheduledPauseRuntimePhase.ACTIVE, runtime().phase)
        coVerify(exactly = 0) { useCase(any(), any(), any(), any(), any()) }
        cleanup(orchestrator)
    }

    @Test
    fun `open checkin persists awaiting and still evaluates automatic checkout`() = runTest {
        remoteResult = AppResult.Success(history(CheckAction.CHECKIN, clock.now().minusSeconds(60)))
        coEvery { useCase(any(), any(), any(), any(), any()) } returns
            AutoActivitiesResult.AccuracyTooLow(null, 80.0, 30)
        val orchestrator = orchestrator(this)

        orchestrator.runOnce(OrchestratorTrigger.PAUSE_START)

        assertEquals(ScheduledPauseRuntimePhase.AWAITING_CHECKOUT, runtime().phase)
        coVerify(exactly = 1) { useCase(any(), any(), any(), any(), any()) }
        verify(exactly = 1) {
            AutoActivityNotifications.postLowAccuracyRetryNotification(any(), any(), any())
        }
        cleanup(orchestrator)
    }

    @Test
    fun `server confirmed checkout enters grace then activates after ten seconds`() = runTest {
        remoteResult = AppResult.Success(history(CheckAction.CHECKIN, clock.now().minusSeconds(60)))
        val orchestrator = orchestrator(this)
        orchestrator.runOnce(OrchestratorTrigger.PAUSE_START)

        val checkout = history(CheckAction.CHECKOUT, clock.now())
        remoteResult = AppResult.Success(checkout)
        orchestrator.onServerConfirmedState("HR70", "P80", checkout)

        assertEquals(ScheduledPauseRuntimePhase.GRACE, runtime().phase)
        verify(exactly = 0) {
            AutoActivityNotifications.postScheduledPauseTransition(any(), true, any())
        }

        clock.instant = clock.instant.plusSeconds(10)
        advanceTimeBy(10_000)
        runCurrent()

        assertEquals(ScheduledPauseRuntimePhase.ACTIVE, runtime().phase)
        verify(exactly = 1) {
            AutoActivityNotifications.postScheduledPauseTransition(any(), true, "pt")
        }
        cleanup(orchestrator)
    }

    @Test
    fun `confirmed checkout without timestamp uses callback time for grace`() = runTest {
        remoteResult = AppResult.Success(history(CheckAction.CHECKIN, clock.now().minusSeconds(60)))
        val orchestrator = orchestrator(this)
        orchestrator.runOnce(OrchestratorTrigger.PAUSE_START)
        val checkoutWithoutTimestamps = history(CheckAction.CHECKOUT, null)

        orchestrator.onServerConfirmedState("HR70", "P80", checkoutWithoutTimestamps)

        val runtime = runtime()
        assertEquals(ScheduledPauseRuntimePhase.GRACE, runtime.phase)
        assertEquals(clock.now().plusSeconds(10).toEpochMilli(), runtime.activateAtEpochMs)
        cleanup(orchestrator)
    }

    @Test
    fun `occurrence ending before full grace is skipped and suppresses one-shot engine reentry`() =
        runTest {
            clock.instant = instantAtLocal(23, 58, 55)
            remoteResult = AppResult.Success(history(CheckAction.CHECKIN, clock.now().minusSeconds(60)))
            val orchestrator = orchestrator(this)
            orchestrator.runOnce(OrchestratorTrigger.PAUSE_START)

            val checkout = history(CheckAction.CHECKOUT, clock.now())
            remoteResult = AppResult.Success(checkout)
            orchestrator.onServerConfirmedState("HR70", "P80", checkout)
            assertEquals(ScheduledPauseRuntimePhase.SKIPPED, runtime().phase)

            orchestrator.runOnce(OrchestratorTrigger.PAUSE_START)

            coVerify(exactly = 1) { useCase(any(), any(), any(), any(), any()) }
            verify(exactly = 0) {
                AutoActivityNotifications.postScheduledPauseTransition(any(), any(), any())
            }
            cleanup(orchestrator)
        }

    @Test
    fun `fresh state failure neither pauses nor enters situation matrix`() = runTest {
        remoteResult = AppResult.Failure(ApiError.Network)
        val orchestrator = orchestrator(this)

        orchestrator.runOnce(OrchestratorTrigger.PAUSE_START)

        assertEquals(ScheduledPauseRuntimePhase.AWAITING_CHECKOUT, runtime().phase)
        coVerify(exactly = 0) { useCase(any(), any(), any(), any(), any()) }
        verify(exactly = 0) {
            AutoActivityNotifications.postScheduledPauseTransition(any(), any(), any())
        }
        cleanup(orchestrator)
    }

    @Test
    fun `verification recovery with no history starts pause immediately without grace`() = runTest {
        remoteResult = AppResult.Failure(ApiError.Network)
        val orchestrator = orchestrator(this)
        orchestrator.runOnce(OrchestratorTrigger.PAUSE_START)
        assertEquals(ScheduledPauseRuntimePhase.AWAITING_CHECKOUT, runtime().phase)

        remoteResult = AppResult.Success(history(null, null))
        orchestrator.runOnce(OrchestratorTrigger.PAUSE_GRACE)

        assertEquals(ScheduledPauseRuntimePhase.ACTIVE, runtime().phase)
        coVerify(exactly = 0) { useCase(any(), any(), any(), any(), any()) }
        cleanup(orchestrator)
    }

    @Test
    fun `confirmed checkin cannot reopen an already active normal pause`() = runTest {
        remoteResult = AppResult.Success(
            history(CheckAction.CHECKOUT, instantAtLocal(0, 0, 0).minusSeconds(60)),
        )
        val orchestrator = orchestrator(this)
        orchestrator.runOnce(OrchestratorTrigger.PAUSE_START)

        orchestrator.onServerConfirmedState(
            "HR70",
            "P80",
            history(CheckAction.CHECKIN, clock.now()),
        )

        assertEquals(ScheduledPauseRuntimePhase.ACTIVE, runtime().phase)
        verify(exactly = 0) {
            AutoActivityNotifications.postScheduledPauseTransition(any(), false, any())
        }
        cleanup(orchestrator)
    }

    @Test
    fun `corrupt runtime is erased instead of being decoded as null forever`() = runTest {
        runtimeJson.value = "{not-json"
        runtimeWrites.clear()
        val orchestrator = orchestrator(this)

        orchestrator.onServerConfirmedState(
            "HR70",
            "P80",
            history(CheckAction.CHECKOUT, clock.now()),
        )

        assertTrue("corrupt payload must be explicitly erased", "" in runtimeWrites)
        assertEquals(ScheduledPauseRuntimePhase.GRACE, runtime().phase)
        cleanup(orchestrator)
    }

    @Test
    fun `automatic activities off clears active runtime flag and all pause state`() = runTest {
        remoteResult = AppResult.Success(
            history(CheckAction.CHECKOUT, instantAtLocal(0, 0, 0).minusSeconds(60)),
        )
        val orchestrator = orchestrator(this)
        orchestrator.runOnce(OrchestratorTrigger.PAUSE_START)
        assertEquals(ScheduledPauseRuntimePhase.ACTIVE, runtime().phase)

        setSettings(automaticActivitiesEnabled = false)
        orchestrator.runOnce(OrchestratorTrigger.PAUSE_START)

        assertEquals("", runtimeJson.value)
        assertEquals(false, pauseFlag.value)
        cleanup(orchestrator)
    }

    @Test
    fun `disabling configured pause cancels persisted grace and its wake`() = runTest {
        remoteResult = AppResult.Success(history(CheckAction.CHECKIN, clock.now().minusSeconds(60)))
        val orchestrator = orchestrator(this)
        orchestrator.runOnce(OrchestratorTrigger.PAUSE_START)
        val checkout = history(CheckAction.CHECKOUT, clock.now())
        orchestrator.onServerConfirmedState("HR70", "P80", checkout)
        assertEquals(ScheduledPauseRuntimePhase.GRACE, runtime().phase)

        setSettings(scheduledPauseEnabled = false)
        orchestrator.onScheduledPauseConfigurationChanged("HR70")

        assertEquals("", runtimeJson.value)
        assertEquals(false, pauseFlag.value)
        cleanup(orchestrator)
    }

    private fun orchestrator(scope: TestScope): BackgroundCheckOrchestrator {
        val wakeLock = mockk<PowerManager.WakeLock>(relaxed = true)
        val powerManager = mockk<PowerManager> {
            every { newWakeLock(any(), any()) } returns wakeLock
        }
        val alarmManager = mockk<AlarmManager>(relaxed = true)
        val context = mockk<Context> {
            every { getSystemService(Context.POWER_SERVICE) } returns powerManager
            every { getSystemService(Context.ALARM_SERVICE) } returns alarmManager
        }
        return BackgroundCheckOrchestrator(
            context = context,
            appPrefs = appPrefs,
            checkRepository = checkRepository,
            runAutomaticActivitiesUseCase = useCase,
            locationProvider = mockk<LocationProvider>(relaxed = true),
            clock = clock,
            authRepository = mockk<AuthRepository>(relaxed = true),
            securePasswordStore = mockk<SecurePasswordStore>(relaxed = true),
            accidentRepository = mockk<AccidentRepository>(relaxed = true),
            activityLogger = mockk(relaxed = true),
            applicationScope = scope,
        )
    }

    private suspend fun cleanup(orchestrator: BackgroundCheckOrchestrator) {
        orchestrator.cancelLowAccuracyRetry()
        orchestrator.resetScheduledPauseContext()
    }

    private fun runtime(): ScheduledPauseRuntimeState =
        json.decodeFromString(runtimeJson.value)

    private fun setSettings(
        automaticActivitiesEnabled: Boolean = true,
        scheduledPauseEnabled: Boolean = true,
        from: String = "00:00",
        to: String = "23:59",
    ) {
        settingsJson.value = json.encodeToString(
            mapOf(
                "HR70" to UserSettings(
                    projects = listOf("P80"),
                    activeProject = "P80",
                    automaticActivitiesEnabled = automaticActivitiesEnabled,
                    scheduledPauseEnabled = scheduledPauseEnabled,
                    scheduledPauseFrom = from,
                    scheduledPauseTo = to,
                    suspendSaturdays = false,
                    suspendSundays = false,
                    notifyActivities = false,
                    notifyScheduledPause = true,
                    notifyAccident = false,
                ),
            ),
        )
    }

    private fun history(action: CheckAction?, at: Instant? = clock.now()): HistoryState {
        val checkinAt = when (action) {
            CheckAction.CHECKIN -> at
            CheckAction.CHECKOUT -> at?.minusSeconds(60)
            null -> null
        }
        val checkoutAt = when (action) {
            CheckAction.CHECKOUT -> at
            CheckAction.CHECKIN -> at?.minusSeconds(120)
            null -> null
        }
        return HistoryState(
            found = action != null,
            chave = "HR70",
            projeto = "P80",
            currentAction = action,
            currentLocal = "Unidade P80",
            hasCurrentDayCheckin = action == CheckAction.CHECKIN,
            lastCheckinAt = checkinAt,
            lastCheckoutAt = checkoutAt,
            transportEnabled = false,
        )
    }

    private class MutableClock(var instant: Instant) : Clock {
        override fun now(): Instant = instant
    }

    companion object {
        private fun instantAtLocal(hour: Int, minute: Int, second: Int): Instant =
            ZonedDateTime.of(
                2026,
                7,
                20,
                hour,
                minute,
                second,
                0,
                ZoneId.systemDefault(),
            ).toInstant()
    }
}
