package br.com.tscode.checking.presentation.check

import android.content.Context
import br.com.tscode.checking.core.error.ApiError
import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.core.time.Clock
import br.com.tscode.checking.data.local.AppPreferencesDataSource
import br.com.tscode.checking.data.local.SecurePasswordStore
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.CheckHistoryEntry
import br.com.tscode.checking.domain.model.InformeType
import br.com.tscode.checking.domain.repository.AuthRepository
import br.com.tscode.checking.domain.repository.CheckRepository
import br.com.tscode.checking.domain.repository.ProjectRepository
import br.com.tscode.checking.domain.usecase.CaptureLocationUseCase
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
 * TP1.1 (plan004 §2.5) — the "ÚLTIMO CHECK-IN/CHECK-OUT" history dialog VM logic. On Success the entries are
 * filtered to the tapped action (check-ins under CHECKIN, check-outs under CHECKOUT), the non-null `local`
 * survives, and `historyDialogError` is false. On Failure `historyDialogError` is true and entries are empty
 * (a load failure is NEVER silently shown as "empty"). `retryHistoryDialog()` re-loads the CURRENT action.
 */
class CheckViewModelHistoryDialogTest {

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

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { appPreferences.language } returns flowOf("pt")
        every { appPreferences.chave } returns flowOf("") // unauthenticated start → init does no network
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = CheckViewModel(
        appPreferences, securePasswordStore, authRepository, projectRepository, checkRepository,
        captureLocationUseCase, orchestrator, offlineCheckQueue, clock, appContext,
        mockk(relaxed = true), mockk(relaxed = true),
    )

    private fun entry(action: CheckAction, local: String?) = CheckHistoryEntry(
        action = action,
        projeto = "P80",
        local = local,
        time = Instant.parse("2026-06-15T01:00:00Z"),
        informe = InformeType.NORMAL,
    )

    private val mixed = listOf(
        entry(CheckAction.CHECKIN, "Área X"),
        entry(CheckAction.CHECKOUT, "Gate 3"),
        entry(CheckAction.CHECKIN, null),
    )

    @Test
    fun `openCheckinHistory success filters to check-ins with location and no error`() = runTest(dispatcher) {
        coEvery { checkRepository.getHistory(any()) } returns AppResult.Success(mixed)
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.openCheckinHistory()
        advanceUntilIdle()

        val s = vm.uiState.value
        assertEquals(CheckDialog.History, s.dialogOpen)
        assertEquals(CheckAction.CHECKIN, s.historyDialogAction)
        assertEquals(2, s.historyDialogEntries.size)
        assertTrue("only check-ins", s.historyDialogEntries.all { it.action == CheckAction.CHECKIN })
        assertEquals("Área X", s.historyDialogEntries[0].local) // non-null location preserved
        assertFalse(s.historyDialogError)
        assertFalse(s.isHistoryDialogLoading)
    }

    @Test
    fun `openCheckoutHistory success filters to check-outs`() = runTest(dispatcher) {
        coEvery { checkRepository.getHistory(any()) } returns AppResult.Success(mixed)
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.openCheckoutHistory()
        advanceUntilIdle()

        val s = vm.uiState.value
        assertEquals(CheckAction.CHECKOUT, s.historyDialogAction)
        assertEquals(1, s.historyDialogEntries.size)
        assertEquals(CheckAction.CHECKOUT, s.historyDialogEntries[0].action)
        assertEquals("Gate 3", s.historyDialogEntries[0].local)
        assertFalse(s.historyDialogError)
    }

    @Test
    fun `history load failure sets error and empty entries (never silent empty)`() = runTest(dispatcher) {
        coEvery { checkRepository.getHistory(any()) } returns AppResult.Failure(ApiError.Network)
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.openCheckinHistory()
        advanceUntilIdle()

        val s = vm.uiState.value
        assertTrue("a load failure must set the error flag", s.historyDialogError)
        assertTrue(s.historyDialogEntries.isEmpty())
        assertFalse(s.isHistoryDialogLoading)
    }

    @Test
    fun `retryHistoryDialog reloads the current action and clears the error`() = runTest(dispatcher) {
        coEvery { checkRepository.getHistory(any()) } returns AppResult.Failure(ApiError.Network)
        val vm = buildViewModel()
        advanceUntilIdle()
        vm.openCheckoutHistory()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.historyDialogError)

        // The repo recovers; retry re-loads the SAME (check-out) action.
        coEvery { checkRepository.getHistory(any()) } returns AppResult.Success(mixed)
        vm.retryHistoryDialog()
        advanceUntilIdle()

        val s = vm.uiState.value
        assertFalse(s.historyDialogError)
        assertEquals(CheckAction.CHECKOUT, s.historyDialogAction)
        assertEquals(1, s.historyDialogEntries.size)
        assertEquals(CheckAction.CHECKOUT, s.historyDialogEntries[0].action)
    }
}
