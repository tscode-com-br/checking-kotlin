package br.com.tscode.checking.presentation.check

import br.com.tscode.checking.core.error.ApiError
import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.domain.model.UserProjects
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserProjectMembershipCoordinatorTest {

    @Test
    fun `last project can be removed and empty membership is committed`() = runTest {
        val response = CompletableDeferred<AppResult<UserProjects>>()
        val calls = mutableListOf<List<String>>()
        val commits = mutableListOf<UserProjects>()

        val coordinator = UserProjectMembershipCoordinator(
            scope = this,
            initialConfirmed = userProjects("alpha"),
            update = { projects ->
                calls += projects
                response.await()
            },
            onStateChanged = {},
            onCommitted = commits::add,
        )

        coordinator.toggle("alpha")

        assertTrue(coordinator.state.syncing)
        assertEquals(emptyList<String>(), coordinator.state.displayed.projects)
        assertEquals("", coordinator.state.displayed.activeProject)

        runCurrent()
        assertEquals(listOf(emptyList<String>()), calls)

        val authoritative = UserProjects(projects = emptyList(), activeProject = "")
        response.complete(AppResult.Success(authoritative))
        advanceUntilIdle()

        assertEquals(authoritative, coordinator.state.displayed)
        assertFalse(coordinator.state.syncing)
        assertEquals(listOf(authoritative), commits)
    }

    @Test
    fun `two rapid toggles are coalesced from optimistic desired state`() = runTest {
        val response = CompletableDeferred<AppResult<UserProjects>>()
        val calls = mutableListOf<List<String>>()

        val coordinator = UserProjectMembershipCoordinator(
            scope = this,
            initialConfirmed = userProjects(),
            update = { projects ->
                calls += projects
                response.await()
            },
            onStateChanged = {},
        )

        coordinator.toggle("alpha")
        coordinator.toggle("beta")

        assertEquals(listOf("alpha", "beta"), coordinator.state.displayed.projects)

        runCurrent()
        assertEquals(listOf(listOf("alpha", "beta")), calls)

        response.complete(AppResult.Success(userProjects("alpha", "beta", active = "beta")))
        advanceUntilIdle()

        assertFalse(coordinator.state.syncing)
        assertEquals(listOf("alpha", "beta"), coordinator.state.displayed.projects)
    }

    @Test
    fun `edit during request is serialized and sent after first PUT`() = runTest {
        val firstResponse = CompletableDeferred<AppResult<UserProjects>>()
        val secondResponse = CompletableDeferred<AppResult<UserProjects>>()
        val calls = mutableListOf<List<String>>()
        val commits = mutableListOf<UserProjects>()
        var inFlight = 0
        var maximumInFlight = 0

        val coordinator = UserProjectMembershipCoordinator(
            scope = this,
            initialConfirmed = userProjects(),
            update = { projects ->
                calls += projects
                inFlight += 1
                maximumInFlight = maxOf(maximumInFlight, inFlight)
                try {
                    if (calls.size == 1) firstResponse.await() else secondResponse.await()
                } finally {
                    inFlight -= 1
                }
            },
            onStateChanged = {},
            onCommitted = commits::add,
        )

        coordinator.toggle("alpha")
        runCurrent()
        assertEquals(listOf(listOf("alpha")), calls)

        coordinator.toggle("beta")
        runCurrent()

        assertEquals(listOf(listOf("alpha")), calls)
        assertEquals(listOf("alpha", "beta"), coordinator.state.displayed.projects)
        assertEquals(1, maximumInFlight)

        firstResponse.complete(AppResult.Success(userProjects("alpha")))
        runCurrent()

        assertEquals(listOf(listOf("alpha"), listOf("alpha", "beta")), calls)
        assertEquals(1, maximumInFlight)
        assertTrue("obsolete success must not run derived commit effects", commits.isEmpty())

        secondResponse.complete(AppResult.Success(userProjects("alpha", "beta")))
        advanceUntilIdle()

        assertEquals(1, maximumInFlight)
        assertFalse(coordinator.state.syncing)
        assertEquals(listOf(userProjects("alpha", "beta")), commits)
    }

    @Test
    fun `syncing stays true through commit and toggle during commit is sent afterwards`() = runTest {
        val commitStarted = CompletableDeferred<Unit>()
        val releaseCommit = CompletableDeferred<Unit>()
        val calls = mutableListOf<List<String>>()
        var commitCount = 0

        val coordinator = UserProjectMembershipCoordinator(
            scope = this,
            initialConfirmed = userProjects(),
            update = { projects ->
                calls += projects
                AppResult.Success(UserProjects(projects, projects.firstOrNull().orEmpty()))
            },
            onStateChanged = {},
            onCommitted = {
                commitCount += 1
                if (commitCount == 1) {
                    commitStarted.complete(Unit)
                    releaseCommit.await()
                }
            },
        )

        coordinator.toggle("alpha")
        runCurrent()
        commitStarted.await()

        assertEquals(listOf(listOf("alpha")), calls)
        assertEquals(listOf("alpha"), coordinator.state.displayed.projects)
        assertTrue("derived commit work is still part of synchronization", coordinator.state.syncing)

        coordinator.toggle("beta")
        runCurrent()

        assertEquals(
            "a second PUT must wait until the first response's derived effects complete",
            listOf(listOf("alpha")),
            calls,
        )
        assertEquals(listOf("alpha", "beta"), coordinator.state.displayed.projects)
        assertTrue(coordinator.state.syncing)

        releaseCommit.complete(Unit)
        advanceUntilIdle()

        assertEquals(listOf(listOf("alpha"), listOf("alpha", "beta")), calls)
        assertEquals(2, commitCount)
        assertEquals(listOf("alpha", "beta"), coordinator.state.displayed.projects)
        assertFalse(coordinator.state.syncing)
    }

    @Test
    fun `final API response replaces optimistic state authoritatively`() = runTest {
        val authoritative = UserProjects(
            projects = listOf("server-project", "audited-project"),
            activeProject = "audited-project",
        )

        val coordinator = UserProjectMembershipCoordinator(
            scope = this,
            initialConfirmed = userProjects("alpha"),
            update = { AppResult.Success(authoritative) },
            onStateChanged = {},
        )

        coordinator.toggle("beta")
        advanceUntilIdle()

        assertEquals(authoritative, coordinator.state.displayed)
        assertFalse(coordinator.state.syncing)
    }

    @Test
    fun `failure of latest intent rolls back and reports error`() = runTest {
        val errors = mutableListOf<ApiError>()
        val states = mutableListOf<UserProjectMembershipSyncState>()
        val initial = userProjects("alpha")

        val coordinator = UserProjectMembershipCoordinator(
            scope = this,
            initialConfirmed = initial,
            update = { AppResult.Failure(ApiError.Network) },
            onStateChanged = states::add,
            onError = errors::add,
        )

        coordinator.toggle("beta")
        assertEquals(listOf("alpha", "beta"), coordinator.state.displayed.projects)

        advanceUntilIdle()

        assertEquals(initial, coordinator.state.displayed)
        assertFalse(coordinator.state.syncing)
        assertEquals(1, errors.size)
        assertSame(ApiError.Network, errors.single())
        assertEquals(initial, states.last().displayed)
    }

    @Test
    fun `failure of obsolete request preserves and submits latest intent`() = runTest {
        val firstResponse = CompletableDeferred<AppResult<UserProjects>>()
        val calls = mutableListOf<List<String>>()
        val errors = mutableListOf<ApiError>()

        val coordinator = UserProjectMembershipCoordinator(
            scope = this,
            initialConfirmed = userProjects(),
            update = { projects ->
                calls += projects
                if (calls.size == 1) {
                    firstResponse.await()
                } else {
                    AppResult.Success(userProjects("alpha", "beta", active = "beta"))
                }
            },
            onStateChanged = {},
            onError = errors::add,
        )

        coordinator.toggle("alpha")
        runCurrent()
        coordinator.toggle("beta")

        firstResponse.complete(AppResult.Failure(ApiError.Network))
        advanceUntilIdle()

        assertEquals(listOf(listOf("alpha"), listOf("alpha", "beta")), calls)
        assertTrue(errors.isEmpty())
        assertEquals(listOf("alpha", "beta"), coordinator.state.displayed.projects)
        assertEquals("beta", coordinator.state.displayed.activeProject)
        assertFalse(coordinator.state.syncing)
    }

    @Test
    fun `reset cancels old session and ignores cancellation-uncooperative response`() = runTest {
        val calls = mutableListOf<List<String>>()
        val commits = mutableListOf<UserProjects>()
        val states = mutableListOf<UserProjectMembershipSyncState>()
        val staleResponse = userProjects("stale-server-project")

        val coordinator = UserProjectMembershipCoordinator(
            scope = this,
            initialConfirmed = userProjects("old-project"),
            update = { projects ->
                calls += projects
                if (calls.size == 1) {
                    try {
                        awaitCancellation()
                    } catch (_: CancellationException) {
                        // Simulates an HTTP adapter that returns a value despite cancellation.
                        AppResult.Success(staleResponse)
                    }
                } else {
                    AppResult.Success(UserProjects(projects, projects.firstOrNull().orEmpty()))
                }
            },
            onStateChanged = states::add,
            onCommitted = commits::add,
        )

        coordinator.toggle("old-extra")
        runCurrent()
        assertEquals(listOf(listOf("old-project", "old-extra")), calls)

        val newSession = userProjects("new-project")
        coordinator.reset(newSession)
        val resetStateIndex = states.lastIndex
        runCurrent()

        assertEquals(newSession, coordinator.state.displayed)
        assertFalse(coordinator.state.syncing)
        assertTrue(commits.isEmpty())
        assertTrue(states.drop(resetStateIndex).none { it.displayed == staleResponse })

        coordinator.toggle("new-extra")
        advanceUntilIdle()

        assertEquals(
            listOf(
                listOf("old-project", "old-extra"),
                listOf("new-project", "new-extra"),
            ),
            calls,
        )
        assertEquals(
            UserProjects(
                projects = listOf("new-project", "new-extra"),
                activeProject = "new-project",
            ),
            coordinator.state.displayed,
        )
        assertEquals(1, commits.size)
    }

    private fun userProjects(
        vararg projects: String,
        active: String = projects.firstOrNull().orEmpty(),
    ) = UserProjects(
        projects = projects.toList(),
        activeProject = active,
    )
}
