package br.com.tscode.checking.platform.background

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.PowerManager
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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class OrchestratorLowAccuracyCancellationRaceTest {

    @Before
    fun setUp() {
        mockkObject(AutoActivityNotifications)
        every {
            AutoActivityNotifications.postLowAccuracyRetryNotification(any(), any(), any())
        } just Runs
        every {
            AutoActivityNotifications.cancelLowAccuracyRetryNotification(any())
        } just Runs

        mockkStatic(PendingIntent::class)
        every {
            PendingIntent.getService(any(), any(), any(), any())
        } returns mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkStatic(PendingIntent::class)
        unmockkObject(AutoActivityNotifications)
    }

    @Test
    fun `cancellation before location use case cannot recreate episode from stale run`() = runTest {
        val appPrefs = mockk<AppPreferencesDataSource>(relaxed = true)
        val checkRepository = mockk<CheckRepository>()
        val useCase = mockk<RunAutomaticActivitiesUseCase>()
        val enteredRemoteState = CompletableDeferred<Unit>()
        val releaseRemoteState = CompletableDeferred<Unit>()

        every { appPrefs.chave } returns flowOf("HR70")
        every { appPrefs.language } returns flowOf("pt")
        every { appPrefs.getFlag(any()) } returns flowOf(false)
        every { appPrefs.userSettingsJson } returns flowOf(
            Json.encodeToString(
                mapOf(
                    "HR70" to UserSettings(
                        projects = listOf("P80"),
                        activeProject = "P80",
                        automaticActivitiesEnabled = true,
                        scheduledPauseEnabled = false,
                        suspendSaturdays = false,
                        suspendSundays = false,
                        notifyAccident = false,
                    ),
                ),
            ),
        )
        coEvery { checkRepository.getLocations() } returns
            AppResult.Success(LocationOptions(emptyList(), 30, 15))
        coEvery { checkRepository.getState("HR70") } coAnswers {
            enteredRemoteState.complete(Unit)
            releaseRemoteState.await()
            AppResult.Success(history())
        }
        coEvery { useCase(any(), any(), any(), any(), any()) } returns
            AutoActivitiesResult.AccuracyTooLow(
                expectedAction = CheckAction.CHECKIN,
                accuracyMeters = 80.0,
                thresholdMeters = 30,
            )

        val wakeLock = mockk<PowerManager.WakeLock>(relaxed = true)
        val powerManager = mockk<PowerManager> {
            every { newWakeLock(any(), any()) } returns wakeLock
        }
        val alarmManager = mockk<AlarmManager>(relaxed = true)
        val context = mockk<Context> {
            every { getSystemService(Context.POWER_SERVICE) } returns powerManager
            every { getSystemService(Context.ALARM_SERVICE) } returns alarmManager
        }
        val clock = mockk<Clock> {
            every { now() } returns Instant.parse("2026-07-20T12:00:00Z")
        }
        val orchestrator = BackgroundCheckOrchestrator(
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
            applicationScope = this,
        )

        val run = launch { orchestrator.runOnce(OrchestratorTrigger.FOREGROUND) }
        enteredRemoteState.await()

        orchestrator.cancelLowAccuracyRetry()
        releaseRemoteState.complete(Unit)
        run.join()

        verify(exactly = 0) {
            AutoActivityNotifications.postLowAccuracyRetryNotification(any(), any(), any())
        }
        advanceTimeBy(LOW_ACCURACY_RETRY_INTERVAL_MILLIS)
        runCurrent()
        coVerify(exactly = 0) { useCase(any(), any(), any(), any(), any()) }

        orchestrator.cancelLowAccuracyRetry()
    }

    private fun history() = HistoryState(
        found = true,
        chave = "HR70",
        projeto = "P80",
        currentAction = CheckAction.CHECKOUT,
        currentLocal = "Unidade P80",
        hasCurrentDayCheckin = false,
        lastCheckinAt = Instant.parse("2026-07-20T10:00:00Z"),
        lastCheckoutAt = Instant.parse("2026-07-20T11:00:00Z"),
        transportEnabled = false,
    )
}
