package br.com.tscode.checking.platform.background

import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.data.local.AppPreferencesDataSource
import br.com.tscode.checking.domain.clientstate.UserSettings
import br.com.tscode.checking.domain.model.AccidentActiveItem
import br.com.tscode.checking.domain.model.AccidentState
import br.com.tscode.checking.domain.repository.AccidentRepository
import br.com.tscode.checking.platform.background.notifications.AutoActivityNotifications
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Proves the DECISION logic that drives the accident push: it fires exactly once per new
 * accident, never re-fires for an accident already seen (dedup across runs via persisted ids),
 * and respects the user's "notify accident" toggle. AutoActivityNotifications is mocked, so this
 * is a pure-JVM test of WHEN the push is posted (the WHETHER-it-renders is covered by the
 * instrumented NotificationMechanismTest).
 */
class AccidentNotificationDecisionTest {

    private val appPrefs = mockk<AppPreferencesDataSource>()
    private val accidentRepo = mockk<AccidentRepository>()

    private val chave = "STSM"

    @Before
    fun setUp() {
        mockkObject(AutoActivityNotifications)
        every { AutoActivityNotifications.postAccidentNotification(any(), any()) } just Runs
        every { appPrefs.chave } returns flowOf(chave)
        every { appPrefs.language } returns flowOf("pt")
        // setSeenAccidentIds returns Preferences (from DataStore.edit); the value is unused here.
        coEvery { appPrefs.setSeenAccidentIds(any()) } returns mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkObject(AutoActivityNotifications)
    }

    private fun orchestrator() = BackgroundCheckOrchestrator(
        context = mockk(relaxed = true),
        appPrefs = appPrefs,
        checkRepository = mockk(relaxed = true),
        runAutomaticActivitiesUseCase = mockk(relaxed = true),
        locationProvider = mockk(relaxed = true),
        clock = mockk(relaxed = true),
        authRepository = mockk(relaxed = true),
        securePasswordStore = mockk(relaxed = true),
        accidentRepository = accidentRepo,
        activityLogger = mockk(relaxed = true),
    )

    private fun successState(vararg ids: Int) = AppResult.Success(
        AccidentState(
            isActive = ids.isNotEmpty(),
            accidentId = ids.firstOrNull(),
            accidentNumberLabel = null,
            projectId = null,
            projectName = null,
            locationName = null,
            description = null,
            awarenessStatus = null,
            currentUserReport = null,
            activeAccidents = ids.map {
                AccidentActiveItem(
                    accidentId = it,
                    accidentNumberLabel = "AC-$it",
                    projectId = 1,
                    projectName = "P80",
                    locationName = "L",
                    description = null,
                    awarenessStatus = "open",
                    currentUserReport = null,
                )
            },
        ),
    )

    @Test
    fun newAccident_postsOnce_andRemembersId() = runTest {
        every { appPrefs.userSettingsJson } returns flowOf("") // empty → defaults, notifyAccident = true
        every { appPrefs.seenAccidentIds } returns flowOf(emptySet())
        coEvery { accidentRepo.getState(chave) } returns successState(42)

        orchestrator().runAccidentCheck()

        verify(exactly = 1) { AutoActivityNotifications.postAccidentNotification(any(), "pt") }
        coVerify(exactly = 1) { appPrefs.setSeenAccidentIds(setOf(42)) }
    }

    @Test
    fun alreadySeenAccident_doesNotPostAgain() = runTest {
        every { appPrefs.userSettingsJson } returns flowOf("")
        every { appPrefs.seenAccidentIds } returns flowOf(setOf(42))
        coEvery { accidentRepo.getState(chave) } returns successState(42)

        orchestrator().runAccidentCheck()

        verify(exactly = 0) { AutoActivityNotifications.postAccidentNotification(any(), any()) }
    }

    @Test
    fun notifyAccidentDisabled_doesNotPost_norEvenQueriesState() = runTest {
        val settings = UserSettings(
            projects = listOf("P80"),
            activeProject = "P80",
            automaticActivitiesEnabled = false,
            notifyAccident = false,
        )
        val json = Json.encodeToString(mapOf(chave to settings))
        every { appPrefs.userSettingsJson } returns flowOf(json)
        every { appPrefs.seenAccidentIds } returns flowOf(emptySet())
        coEvery { accidentRepo.getState(any()) } returns successState(42)

        orchestrator().runAccidentCheck()

        verify(exactly = 0) { AutoActivityNotifications.postAccidentNotification(any(), any()) }
        coVerify(exactly = 0) { accidentRepo.getState(any()) }
    }
}
