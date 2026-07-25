package br.com.tscode.checking.presentation.privacy

import android.content.Context
import br.com.tscode.checking.data.local.AppPreferencesDataSource
import br.com.tscode.checking.data.local.SecurePasswordStore
import br.com.tscode.checking.data.local.activitylog.ActivityLog
import br.com.tscode.checking.domain.repository.AuthRepository
import br.com.tscode.checking.platform.background.AutoActivityController
import br.com.tscode.checking.platform.background.BackgroundCheckOrchestrator
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PrivacyViewModelCleanupTest {

    private val dispatcher = StandardTestDispatcher()
    private val appPrefs = mockk<AppPreferencesDataSource>(relaxed = true)
    private val securePasswordStore = mockk<SecurePasswordStore>(relaxed = true)
    private val activityLog = mockk<ActivityLog>(relaxed = true)
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val orchestrator = mockk<BackgroundCheckOrchestrator>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { appPrefs.language } returns flowOf("pt")
        every { appPrefs.chave } returns flowOf("HR70")
        every { orchestrator.cancelLowAccuracyRetry() } just Runs
        coEvery { orchestrator.resetScheduledPauseContext() } just Runs
        mockkObject(AutoActivityController)
        every { AutoActivityController.stop(any()) } just Runs
    }

    @After
    fun tearDown() {
        unmockkObject(AutoActivityController)
        Dispatchers.resetMain()
    }

    @Test
    fun `local wipe tears down retry and pause runtime before clearing preferences`() =
        runTest(dispatcher) {
            var completed = false
            val viewModel = PrivacyViewModel(
                appPrefs = appPrefs,
                securePasswordStore = securePasswordStore,
                activityLog = activityLog,
                authRepository = authRepository,
                orchestrator = orchestrator,
                appContext = context,
            )

            viewModel.deleteLocalData { completed = true }
            advanceUntilIdle()

            coVerifyOrder {
                orchestrator.cancelLowAccuracyRetry()
                orchestrator.resetScheduledPauseContext()
                appPrefs.clearAll()
            }
            verify(exactly = 1) { AutoActivityController.stop(context) }
            coVerify(exactly = 1) { authRepository.logout() }
            assertTrue(completed)
        }
}
