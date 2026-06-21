package br.com.tscode.checking.presentation.check

import android.content.Context
import br.com.tscode.checking.core.time.Clock
import br.com.tscode.checking.data.local.AppPreferencesDataSource
import br.com.tscode.checking.data.local.SecurePasswordStore
import br.com.tscode.checking.data.local.activitylog.ActivityLog
import br.com.tscode.checking.domain.model.ActivityActor
import br.com.tscode.checking.domain.model.ActivityKind
import br.com.tscode.checking.domain.model.ActivityLogEntry
import br.com.tscode.checking.domain.model.ActivitySeverity
import br.com.tscode.checking.domain.repository.AuthRepository
import br.com.tscode.checking.domain.repository.CheckRepository
import br.com.tscode.checking.domain.repository.ProjectRepository
import br.com.tscode.checking.domain.usecase.CaptureLocationUseCase
import br.com.tscode.checking.platform.activitylog.ActivityLogger
import br.com.tscode.checking.platform.background.BackgroundCheckOrchestrator
import br.com.tscode.checking.platform.background.offline.OfflineCheckQueue
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

/**
 * plan004 EP8 (§3.7) — Activities log paging in the ViewModel. openActivitiesDialog loads page 0 of 30 and
 * sets canLoadMore; loadMoreActivities appends the next 30, advances the offset, and STOPS on a short page;
 * once the list is exhausted, further calls are no-ops (re-entrancy / end guarded by isActivitiesLoading +
 * activityCanLoadMore). A controllable (non-relaxed) ActivityLog mock returns the pages.
 */
class CheckViewModelActivitiesPagingTest {

    private val dispatcher = StandardTestDispatcher()

    private val appPreferences = mockk<AppPreferencesDataSource>(relaxed = true)
    private val securePasswordStore = mockk<SecurePasswordStore>(relaxed = true)
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val projectRepository = mockk<ProjectRepository>(relaxed = true)
    private val checkRepository = mockk<CheckRepository>(relaxed = true)
    private val captureLocationUseCase = mockk<CaptureLocationUseCase>(relaxed = true)
    private val orchestrator = mockk<BackgroundCheckOrchestrator>(relaxed = true)
    private val offlineCheckQueue = mockk<OfflineCheckQueue>(relaxed = true)
    private val clock = mockk<Clock> { every { now() } returns Instant.EPOCH }
    private val appContext = mockk<Context>(relaxed = true)
    private val activityLogger = mockk<ActivityLogger>(relaxed = true)
    private val activityLog = mockk<ActivityLog>()

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        // Unauthenticated start (empty chave) → init does no auth/network. Stored language avoids the
        // device-locale fallback (an Android stub in JVM tests). Mirrors CheckViewModelForegroundTest.
        every { appPreferences.language } returns flowOf("pt")
        every { appPreferences.chave } returns flowOf("")
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = CheckViewModel(
        appPreferences, securePasswordStore, authRepository, projectRepository, checkRepository,
        captureLocationUseCase, orchestrator, offlineCheckQueue, clock, appContext,
        activityLogger, activityLog,
    )

    private fun rows(n: Int) = List(n) {
        ActivityLogEntry(Instant.EPOCH, ActivityActor.SYS, ActivityKind.SYSTEM, ActivitySeverity.INFO, "row")
    }

    @Test
    fun `openActivitiesDialog loads first page of 30 and sets canLoadMore`() = runTest(dispatcher) {
        coEvery { activityLog.page(0, ActivityLog.PAGE_SIZE) } returns rows(30)
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.openActivitiesDialog()
        advanceUntilIdle()

        val s = vm.uiState.value
        assertEquals(CheckDialog.Activities, s.dialogOpen)
        assertEquals(30, s.activityEntries.size)
        assertEquals(30, s.activityNextOffset)
        assertTrue("a full page means more may exist", s.activityCanLoadMore)
        assertFalse(s.isActivitiesLoading)
    }

    @Test
    fun `loadMoreActivities appends the next page and stops on a short page`() = runTest(dispatcher) {
        coEvery { activityLog.page(0, ActivityLog.PAGE_SIZE) } returns rows(30)
        coEvery { activityLog.page(30, ActivityLog.PAGE_SIZE) } returns rows(30)
        coEvery { activityLog.page(60, ActivityLog.PAGE_SIZE) } returns rows(10) // short page → end of log
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.openActivitiesDialog(); advanceUntilIdle()

        vm.loadMoreActivities(); advanceUntilIdle()
        assertEquals(60, vm.uiState.value.activityEntries.size)
        assertEquals(60, vm.uiState.value.activityNextOffset)
        assertTrue(vm.uiState.value.activityCanLoadMore)

        vm.loadMoreActivities(); advanceUntilIdle()
        assertEquals(70, vm.uiState.value.activityEntries.size)
        assertFalse("a short page means no more pages", vm.uiState.value.activityCanLoadMore)

        // Exhausted: further calls are no-ops (guarded by activityCanLoadMore == false).
        vm.loadMoreActivities(); advanceUntilIdle()
        assertEquals(70, vm.uiState.value.activityEntries.size)
    }
}
