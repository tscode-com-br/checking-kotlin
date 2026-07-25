package br.com.tscode.checking.presentation.check

import android.content.Context
import br.com.tscode.checking.core.error.ApiError
import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.core.time.Clock
import br.com.tscode.checking.data.local.AppPreferencesDataSource
import br.com.tscode.checking.data.local.SecurePasswordStore
import br.com.tscode.checking.domain.model.AuthStatus
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.HistoryState
import br.com.tscode.checking.domain.model.LocationMatch
import br.com.tscode.checking.domain.model.MatchStatus
import br.com.tscode.checking.domain.model.UserProjects
import br.com.tscode.checking.domain.repository.AuthRepository
import br.com.tscode.checking.domain.repository.CheckRepository
import br.com.tscode.checking.domain.repository.ProjectRepository
import br.com.tscode.checking.domain.usecase.CaptureLocationUseCase
import br.com.tscode.checking.platform.background.BackgroundCheckOrchestrator
import br.com.tscode.checking.platform.background.offline.OfflineCheckQueue
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class CheckViewModelManualLowAccuracySubmitTest {

    private val dispatcher = StandardTestDispatcher()
    private val appPreferences = mockk<AppPreferencesDataSource>(relaxed = true)
    private val checkRepository = mockk<CheckRepository>(relaxed = true)
    private val orchestrator = mockk<BackgroundCheckOrchestrator>(relaxed = true)
    private val offlineQueue = mockk<OfflineCheckQueue>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { appPreferences.language } returns flowOf("pt")
        every { appPreferences.chave } returns flowOf("")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `successful manual fallback submit cancels accuracy retry`() = runTest(dispatcher) {
        val viewModel = readyViewModel()
        coEvery { checkRepository.submit(any(), any(), any(), any(), any(), any(), any(), any()) } returns
            AppResult.Success(submittedState())
        coEvery { checkRepository.getState("1234") } returns AppResult.Failure(ApiError.Network)

        viewModel.onSubmit()
        advanceUntilIdle()

        verify(exactly = 1) { orchestrator.cancelLowAccuracyRetry() }
        coVerify(exactly = 0) { offlineQueue.enqueue(any()) }
    }

    @Test
    fun `queued offline manual fallback submit cancels only after enqueue`() = runTest(dispatcher) {
        val events = mutableListOf<String>()
        val viewModel = readyViewModel()
        coEvery { checkRepository.submit(any(), any(), any(), any(), any(), any(), any(), any()) } returns
            AppResult.Failure(ApiError.Network)
        coEvery { offlineQueue.enqueue(any()) } answers {
            events += "enqueue"
        }
        every { orchestrator.cancelLowAccuracyRetry() } answers {
            events += "cancel"
        }

        viewModel.onSubmit()
        advanceUntilIdle()

        coVerify(exactly = 1) { offlineQueue.enqueue(any()) }
        verify(exactly = 1) { orchestrator.cancelLowAccuracyRetry() }
        assertEquals(listOf("enqueue", "cancel"), events)
    }

    @Test
    fun `failed HTTP manual fallback submit keeps accuracy retry active`() = runTest(dispatcher) {
        val viewModel = readyViewModel()
        coEvery { checkRepository.submit(any(), any(), any(), any(), any(), any(), any(), any()) } returns
            AppResult.Failure(ApiError.Http(500, "boom"))

        viewModel.onSubmit()
        advanceUntilIdle()

        verify(exactly = 0) { orchestrator.cancelLowAccuracyRetry() }
        coVerify(exactly = 0) { offlineQueue.enqueue(any()) }
    }

    private suspend fun TestScope.readyViewModel(): CheckViewModel {
        val viewModel = CheckViewModel(
            appPreferences = appPreferences,
            securePasswordStore = mockk<SecurePasswordStore>(relaxed = true),
            authRepository = mockk<AuthRepository>(relaxed = true),
            projectRepository = mockk<ProjectRepository>(relaxed = true),
            checkRepository = checkRepository,
            captureLocationUseCase = mockk<CaptureLocationUseCase>(relaxed = true),
            orchestrator = orchestrator,
            offlineCheckQueue = offlineQueue,
            clock = mockk<Clock> { every { now() } returns Instant.EPOCH },
            appContext = mockk<Context>(relaxed = true),
            activityLogger = mockk(relaxed = true),
            activityLog = mockk(relaxed = true),
        )
        advanceUntilIdle()
        replaceUiState(viewModel, lowAccuracyFallbackState())
        return viewModel
    }

    @Suppress("UNCHECKED_CAST")
    private fun replaceUiState(viewModel: CheckViewModel, state: CheckUiState) {
        val field = CheckViewModel::class.java.getDeclaredField("_uiState")
        field.isAccessible = true
        (field.get(viewModel) as MutableStateFlow<CheckUiState>).value = state
    }

    private fun lowAccuracyFallbackState() = CheckUiState(
        chave = "1234",
        authStatus = AuthStatus(
            found = true,
            chave = "1234",
            hasPassword = true,
            authenticated = true,
            message = "",
        ),
        userProjects = UserProjects(listOf("P80"), "P80"),
        automaticActivitiesEnabled = true,
        locationMatch = LocationMatch(
            matched = false,
            resolvedLocal = null,
            label = "",
            status = MatchStatus.ACCURACY_TOO_LOW,
            message = "",
            accuracyMeters = 75.0,
            accuracyThresholdMeters = 30,
            minimumCheckoutDistanceMeters = 2_000,
            nearestWorkplaceDistanceMeters = null,
        ),
        selectedManualLocation = "Unidade P80",
        selectedAction = CheckAction.CHECKIN,
    )

    private fun submittedState() = HistoryState(
        found = true,
        chave = "1234",
        projeto = "P80",
        currentAction = CheckAction.CHECKIN,
        currentLocal = "Unidade P80",
        hasCurrentDayCheckin = true,
        lastCheckinAt = Instant.EPOCH,
        lastCheckoutAt = null,
        transportEnabled = false,
    )
}
