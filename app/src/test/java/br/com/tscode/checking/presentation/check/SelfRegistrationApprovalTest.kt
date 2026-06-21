package br.com.tscode.checking.presentation.check

import android.content.Context
import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.core.time.Clock
import br.com.tscode.checking.data.local.AppPreferencesDataSource
import br.com.tscode.checking.data.local.SecurePasswordStore
import br.com.tscode.checking.domain.model.AuthStatus
import br.com.tscode.checking.domain.model.Project
import br.com.tscode.checking.domain.repository.AuthRepository
import br.com.tscode.checking.domain.repository.CheckRepository
import br.com.tscode.checking.domain.repository.ProjectRepository
import br.com.tscode.checking.domain.usecase.CaptureLocationUseCase
import br.com.tscode.checking.platform.background.BackgroundCheckOrchestrator
import br.com.tscode.checking.platform.background.OrchestratorTrigger
import br.com.tscode.checking.platform.background.offline.OfflineCheckQueue
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import br.com.tscode.checking.i18n.t as i18nText

/**
 * TP4 (#2, #3) — CheckViewModel approval state machine (mocked repos; StandardTestDispatcher; runTest).
 *
 * Entering "awaiting" starts a 20 s polling loop (startPendingApprovalPolling). That loop re-arms forever
 * while getStatus keeps returning pending, so advanceUntilIdle() would spin. The awaiting tests therefore
 * settle the triggering coroutine with runCurrent() (which does NOT advance past the 20 s delay), assert,
 * then cancel the poll via onChaveChanged("") (which calls stopPendingApprovalPolling synchronously)
 * before letting runTest drain.
 *
 * The full auth-success fan-out (onAuthenticationSucceeded → ensureEngineRunningIfEligible + history +
 * projects + SSE) hits Android statics when auto-activities are ON and is heavy to drive in a JVM test
 * (same scoping as CheckViewModelForegroundTest). The "approval" case therefore asserts the new behaviour —
 * approval detected ⇒ VM logs in with the stored password — up to the login() call; the login→engine link
 * is the pre-existing authenticated path (compile-verified EP9, on-device TP5/TP9).
 */
class SelfRegistrationApprovalTest {

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
        every { appPreferences.chave } returns flowOf("")
        // init → loadUserSettings reads this; a relaxed empty flow would throw on .first() and kill init.
        every { appPreferences.userSettingsJson } returns flowOf("{}")
        coEvery { authRepository.logout() } returns AppResult.Success(Unit)
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = CheckViewModel(
        appPreferences, securePasswordStore, authRepository, projectRepository, checkRepository,
        captureLocationUseCase, orchestrator, offlineCheckQueue, clock, appContext,
        mockk(relaxed = true), mockk(relaxed = true),
    )

    private fun status(
        found: Boolean = false, hasPassword: Boolean = false, authenticated: Boolean = false,
        pendingApproval: Boolean = false, queueFull: Boolean = false, chave: String = "NEW1",
    ) = AuthStatus(found, chave, hasPassword, authenticated, "m", pendingApproval, queueFull)

    private fun expected(key: String) = i18nText(key, null, "pt")

    // ── #2 submit → pending ───────────────────────────────────────────────────────────────────────
    @Test
    fun submit_pending_enters_awaiting_red_bar_password_stored_not_authenticated() = runTest(dispatcher) {
        coEvery { authRepository.getStatus("NEW1") } returns AppResult.Success(status(found = false))
        coEvery { projectRepository.listProjects() } returns AppResult.Success(listOf(Project(1, "PRJ", false)))
        coEvery { authRepository.selfRegister(any(), any(), any(), any(), any(), any()) } returns
            AppResult.Success(status(found = false, pendingApproval = true))
        val vm = buildViewModel()
        advanceUntilIdle()
        vm.onChaveChanged("NEW1"); advanceUntilIdle()
        vm.loadProjectCatalogForRegistration(); advanceUntilIdle()
        vm.onRegProjectToggled(1)
        vm.onRegNomeChanged("Full Name")
        vm.onRegPasswordChanged("abc123")
        vm.onRegConfirmPwChanged("abc123")

        vm.submitSelfRegistration()
        runCurrent() // settle submit; the 20 s poll stays suspended (do NOT advanceUntilIdle here)

        val s = vm.uiState.value
        assertTrue("must enter awaiting", s.isAwaitingApproval)
        assertEquals(expected("auth.awaitingApproval"), s.notificationPrimary)
        assertEquals(NotificationTone.Error, s.notificationTone)
        assertFalse(s.isAuthenticated)
        assertFalse("pending/unauthenticated → cannot submit a check", s.canSubmit)
        verify { securePasswordStore.setPassword("NEW1", "abc123") }
        coVerify(exactly = 0) { authRepository.getHistory(any()) } // onAuthenticationSucceeded NOT called
        coVerify(exactly = 0) { orchestrator.runOnce(any()) }      // engine NOT run

        vm.onChaveChanged(""); advanceUntilIdle() // cancel the poll → clean test teardown
    }

    // ── #2 submit → queue_full ────────────────────────────────────────────────────────────────────
    @Test
    fun submit_queue_full_red_message_not_awaiting_not_authenticated() = runTest(dispatcher) {
        coEvery { authRepository.getStatus("NEW1") } returns AppResult.Success(status(found = false))
        coEvery { projectRepository.listProjects() } returns AppResult.Success(listOf(Project(1, "PRJ", false)))
        coEvery { authRepository.selfRegister(any(), any(), any(), any(), any(), any()) } returns
            AppResult.Success(status(found = false, queueFull = true))
        val vm = buildViewModel()
        advanceUntilIdle()
        vm.onChaveChanged("NEW1"); advanceUntilIdle()
        vm.loadProjectCatalogForRegistration(); advanceUntilIdle()
        vm.onRegProjectToggled(1)
        vm.onRegNomeChanged("Full Name")
        vm.onRegPasswordChanged("abc123")
        vm.onRegConfirmPwChanged("abc123")

        vm.submitSelfRegistration(); advanceUntilIdle() // queue_full → no poll → safe

        val s = vm.uiState.value
        assertEquals(expected("auth.registrationQueueFull"), s.notificationPrimary)
        assertEquals(NotificationTone.Error, s.notificationTone)
        assertFalse(s.isAwaitingApproval)
        assertFalse(s.isAuthenticated)
    }

    // ── #2 status probe → pending (e.g. resume of a known-pending key) + guard #3 (canSubmit) ───────
    @Test
    fun probe_pending_enters_awaiting_and_blocks_submit() = runTest(dispatcher) {
        coEvery { authRepository.getStatus("PND1") } returns
            AppResult.Success(status(found = false, pendingApproval = true, chave = "PND1"))
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.onChaveChanged("PND1")
        runCurrent() // probeStatus → pending → awaiting; poll suspended

        val s = vm.uiState.value
        assertTrue(s.isAwaitingApproval)
        assertEquals(expected("auth.awaitingApproval"), s.notificationPrimary)
        assertEquals(NotificationTone.Error, s.notificationTone)
        assertFalse(s.canSubmit)

        vm.onChaveChanged(""); advanceUntilIdle()
    }

    // ── #2 approval: found flips true → VM logs in with the stored password ─────────────────────────
    @Test
    fun approval_found_true_triggers_login_with_stored_password() = runTest(dispatcher) {
        every { securePasswordStore.getPassword("APR1") } returns "pw1234"
        coEvery { authRepository.getStatus("APR1") } returns
            AppResult.Success(status(found = true, hasPassword = true, chave = "APR1"))
        // authenticated=false keeps the heavy onAuthenticationSucceeded fan-out out of this JVM test.
        coEvery { authRepository.login("APR1", "pw1234") } returns
            AppResult.Success(status(found = true, hasPassword = true, authenticated = false, chave = "APR1"))
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.onChaveChanged("APR1"); advanceUntilIdle()

        coVerify { authRepository.login("APR1", "pw1234") }
        assertFalse(vm.uiState.value.isAwaitingApproval) // approved → no longer awaiting
    }

    // ── #2 rejection (silent) + auto-open: unknown key → form opens, no error message ───────────────
    @Test
    fun unknown_key_autoopens_registration_silently() = runTest(dispatcher) {
        coEvery { authRepository.getStatus("UNK1") } returns
            AppResult.Success(status(found = false, chave = "UNK1"))
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.onChaveChanged("UNK1"); advanceUntilIdle()

        val s = vm.uiState.value
        assertEquals(CheckDialog.SelfRegistration, s.dialogOpen) // auto-opened once
        assertFalse(s.isAwaitingApproval)
        assertEquals(NotificationTone.None, s.notificationTone)  // decision 4 — no message on rejection
        assertEquals("", s.notificationPrimary)
    }

    // ── #2 auto-open is suppressed after the user dismisses the form (dismissedAssistanceForChave) ───
    @Test
    fun dismiss_sets_guard_and_does_not_reopen_on_foreground() = runTest(dispatcher) {
        coEvery { authRepository.getStatus("UNK2") } returns
            AppResult.Success(status(found = false, chave = "UNK2"))
        val vm = buildViewModel()
        advanceUntilIdle()
        vm.onChaveChanged("UNK2"); advanceUntilIdle()
        assertEquals(CheckDialog.SelfRegistration, vm.uiState.value.dialogOpen)

        vm.dismissDialog()
        assertEquals(null, vm.uiState.value.dialogOpen)

        vm.onForegroundResume(); advanceUntilIdle() // not awaiting/authenticated → must not reopen
        assertEquals(null, vm.uiState.value.dialogOpen)
    }

    // ── #2 restart: stored key that is still pending → awaiting reconstructed at init ────────────────
    @Test
    fun restart_with_stored_pending_key_reconstructs_awaiting() = runTest(dispatcher) {
        every { appPreferences.chave } returns flowOf("RST1")
        every { securePasswordStore.getPassword("RST1") } returns ""
        coEvery { authRepository.getStatus("RST1") } returns
            AppResult.Success(status(found = false, pendingApproval = true, chave = "RST1"))
        val vm = buildViewModel()
        runCurrent() // init → probeStatus → pending → awaiting; poll suspended

        assertTrue(vm.uiState.value.isAwaitingApproval)

        vm.onChaveChanged(""); advanceUntilIdle()
    }

    // ── #3 guard: while awaiting, foreground re-probes (no orchestrator/engine run) ──────────────────
    @Test
    fun awaiting_foreground_resume_reprobes_without_running_orchestrator() = runTest(dispatcher) {
        coEvery { authRepository.getStatus("AWF1") } returns
            AppResult.Success(status(found = false, pendingApproval = true, chave = "AWF1"))
        val vm = buildViewModel()
        advanceUntilIdle()
        vm.onChaveChanged("AWF1"); runCurrent()
        assertTrue(vm.uiState.value.isAwaitingApproval)

        vm.onForegroundResume(); runCurrent() // awaiting branch → probeStatus, NOT orchestrator

        coVerify(exactly = 0) { orchestrator.runOnce(any()) }

        vm.onChaveChanged(""); advanceUntilIdle()
    }
}
