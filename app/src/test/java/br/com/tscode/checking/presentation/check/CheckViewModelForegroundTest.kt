package br.com.tscode.checking.presentation.check

import android.content.Context
import br.com.tscode.checking.core.time.Clock
import br.com.tscode.checking.data.local.AppPreferencesDataSource
import br.com.tscode.checking.data.local.SecurePasswordStore
import br.com.tscode.checking.domain.repository.AuthRepository
import br.com.tscode.checking.domain.repository.CheckRepository
import br.com.tscode.checking.domain.repository.ProjectRepository
import br.com.tscode.checking.domain.usecase.CaptureLocationUseCase
import br.com.tscode.checking.platform.background.BackgroundCheckOrchestrator
import br.com.tscode.checking.platform.background.OrchestratorTrigger
import br.com.tscode.checking.platform.background.offline.OfflineCheckQueue
import io.mockk.coVerify
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
import org.junit.Before
import org.junit.Test
import java.time.Instant

/**
 * TP4 (test 1) — change C foreground-trigger gate in CheckViewModel.onForegroundResume().
 *
 * Covered here (JVM-clean): NOT authenticated → the orchestrator is NOT run. The two AUTHENTICATED
 * sub-cases (auto ON → runs FOREGROUND; auto OFF → does not) require driving the VM's auth-success path,
 * which invokes Android statics (PermissionLadder / AutoActivityController) and a chain of sealed-result
 * repo calls — disproportionate for a JVM unit test. Those are covered by the EP3 gate code + the
 * engine-level no-duplicate proofs (TP1 4a/6a, TP3) and are verified on-device in TP9.
 */
class CheckViewModelForegroundTest {

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
        // Unauthenticated start: empty stored chave → init does no auth, no network. A stored language
        // ("pt") avoids the device-locale fallback (LocaleList.getDefault() is an Android stub in JVM tests).
        every { appPreferences.language } returns flowOf("pt")
        every { appPreferences.chave } returns flowOf("")
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = CheckViewModel(
        appPreferences, securePasswordStore, authRepository, projectRepository, checkRepository,
        captureLocationUseCase, orchestrator, offlineCheckQueue, clock, appContext,
    )

    @Test
    fun `onForegroundResume_when_not_authenticated_does_not_run_orchestrator`() = runTest(dispatcher) {
        val vm = buildViewModel()
        advanceUntilIdle() // let init settle (isInitializing=false, no auth)

        vm.onForegroundResume()
        advanceUntilIdle()

        coVerify(exactly = 0) { orchestrator.runOnce(any()) }
        coVerify(exactly = 0) { orchestrator.runOnce(OrchestratorTrigger.FOREGROUND) }
    }
}
