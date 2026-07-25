package br.com.tscode.checking.presentation.check

import android.content.Context
import br.com.tscode.checking.core.error.ApiError
import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.core.time.Clock
import br.com.tscode.checking.data.local.AppPreferencesDataSource
import br.com.tscode.checking.data.local.SecurePasswordStore
import br.com.tscode.checking.domain.model.AuthStatus
import br.com.tscode.checking.domain.model.LocationOptions
import br.com.tscode.checking.domain.model.UserProjects
import br.com.tscode.checking.domain.repository.AuthRepository
import br.com.tscode.checking.domain.repository.CheckRepository
import br.com.tscode.checking.domain.repository.ProjectRepository
import br.com.tscode.checking.domain.usecase.CaptureLocationUseCase
import br.com.tscode.checking.platform.background.BackgroundCheckOrchestrator
import br.com.tscode.checking.platform.background.offline.OfflineCheckQueue
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class CheckViewModelProjectMembershipTest {

    private val dispatcher = StandardTestDispatcher()
    private val appPreferences = mockk<AppPreferencesDataSource>(relaxed = true)
    private val securePasswordStore = mockk<SecurePasswordStore>(relaxed = true)
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val projectRepository = mockk<ProjectRepository>(relaxed = true)
    private val checkRepository = mockk<CheckRepository>(relaxed = true)
    private val captureLocationUseCase = mockk<CaptureLocationUseCase>(relaxed = true)
    private val orchestrator = mockk<BackgroundCheckOrchestrator>(relaxed = true)
    private val offlineQueue = mockk<OfflineCheckQueue>(relaxed = true)
    private val appContext = mockk<Context>(relaxed = true)
    private val clock = mockk<Clock> { every { now() } returns Instant.EPOCH }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { appPreferences.language } returns flowOf("pt")
        every { appPreferences.chave } returns flowOf("1234")
        every { appPreferences.userSettingsJson } returns flowOf("{}")
        every { appPreferences.getFlag(any()) } returns flowOf(false)
        every { securePasswordStore.getPassword("1234") } returns "123"
        every { checkRepository.streamEvents("1234") } returns emptyFlow()

        coEvery { authRepository.getStatus("1234") } returns
            AppResult.Success(authenticatedStatus(authenticated = false))
        coEvery { authRepository.login("1234", "123") } returns
            AppResult.Success(authenticatedStatus(authenticated = true))
        coEvery { authRepository.getHistory("1234") } returns AppResult.Failure(ApiError.Network)
        coEvery { projectRepository.listProjects() } returns AppResult.Success(emptyList())
        coEvery { checkRepository.getLocations() } returns
            AppResult.Success(LocationOptions(emptyList(), 30, 15))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `authoritative empty GET shows exact message and finishes loading`() = runTest(dispatcher) {
        coEvery { projectRepository.getUserProjects() } returns
            AppResult.Success(UserProjects(emptyList(), ""))

        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals(emptyList<String>(), viewModel.uiState.value.userProjects?.projects)
        assertFalse(viewModel.uiState.value.isProjectsLoading)
        assertFalse(viewModel.uiState.value.isProjectMembershipSyncing)
        assertEquals(
            "O usuário não está cadastrado em nenhum projeto.",
            viewModel.uiState.value.notificationPrimary,
        )
    }

    @Test
    fun `removing final checkbox sends empty list and keeps authoritative empty response`() =
        runTest(dispatcher) {
            coEvery { projectRepository.getUserProjects() } returns
                AppResult.Success(UserProjects(listOf("P80"), "P80"))
            val updates = mutableListOf<List<String>>()
            coEvery { projectRepository.updateUserProjects(any()) } coAnswers {
                firstArg<List<String>>().also(updates::add)
                AppResult.Success(UserProjects(emptyList(), ""))
            }

            val viewModel = buildViewModel()
            advanceUntilIdle()
            viewModel.onProjectMembershipToggled("P80")
            advanceUntilIdle()

            assertEquals(listOf(emptyList<String>()), updates)
            assertEquals(UserProjects(emptyList(), ""), viewModel.uiState.value.userProjects)
            assertFalse(viewModel.uiState.value.isProjectMembershipSyncing)
            assertEquals(
                "O usuário não está cadastrado em nenhum projeto.",
                viewModel.uiState.value.notificationPrimary,
            )
        }

    @Test
    fun `two immediate checkbox taps are coalesced into complete desired membership`() =
        runTest(dispatcher) {
            coEvery { projectRepository.getUserProjects() } returns
                AppResult.Success(UserProjects(emptyList(), ""))
            val updates = mutableListOf<List<String>>()
            coEvery { projectRepository.updateUserProjects(any()) } coAnswers {
                val projects = firstArg<List<String>>().also(updates::add)
                AppResult.Success(UserProjects(projects, projects.firstOrNull().orEmpty()))
            }

            val viewModel = buildViewModel()
            advanceUntilIdle()
            viewModel.onProjectMembershipToggled("P80")
            viewModel.onProjectMembershipToggled("P83")
            advanceUntilIdle()

            assertEquals(listOf(listOf("P80", "P83")), updates)
            assertEquals(listOf("P80", "P83"), viewModel.uiState.value.userProjects?.projects)
            assertFalse(viewModel.uiState.value.isProjectMembershipSyncing)
        }

    @Test
    fun `active project selection resets scheduled pause context through common reconciliation`() =
        runTest(dispatcher) {
            coEvery { projectRepository.getUserProjects() } returns
                AppResult.Success(UserProjects(listOf("P80", "P83"), "P80"))
            coEvery { projectRepository.updateActiveProject("P83") } returns
                AppResult.Success(UserProjects(listOf("P80", "P83"), "P83"))
            val viewModel = buildViewModel()
            advanceUntilIdle()
            clearMocks(orchestrator, answers = false, recordedCalls = true)

            viewModel.onActiveProjectSelected("P83")
            advanceUntilIdle()

            assertEquals("P83", viewModel.uiState.value.userProjects?.activeProject)
            coVerify(exactly = 1) { orchestrator.resetScheduledPauseContext() }
        }

    private fun buildViewModel() = CheckViewModel(
        appPreferences = appPreferences,
        securePasswordStore = securePasswordStore,
        authRepository = authRepository,
        projectRepository = projectRepository,
        checkRepository = checkRepository,
        captureLocationUseCase = captureLocationUseCase,
        orchestrator = orchestrator,
        offlineCheckQueue = offlineQueue,
        clock = clock,
        appContext = appContext,
        activityLogger = mockk(relaxed = true),
        activityLog = mockk(relaxed = true),
    )

    private fun authenticatedStatus(authenticated: Boolean) = AuthStatus(
        found = true,
        chave = "1234",
        hasPassword = true,
        authenticated = authenticated,
        message = "",
    )
}
