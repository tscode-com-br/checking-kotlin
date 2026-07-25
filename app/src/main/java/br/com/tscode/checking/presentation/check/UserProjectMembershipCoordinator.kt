package br.com.tscode.checking.presentation.check

import br.com.tscode.checking.core.error.ApiError
import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.domain.model.UserProjects
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * State exposed to the projects selector.
 *
 * [displayed] is optimistic while [syncing] is true. Once the last update succeeds it is replaced by
 * the authoritative response returned by the API.
 */
data class UserProjectMembershipSyncState(
    val displayed: UserProjects,
    val syncing: Boolean,
)

/**
 * Serializes changes to a user's project memberships.
 *
 * A single worker owns all PUTs, while calls to [toggle] keep changing the desired (optimistic) list.
 * If a newer intent arrives during a PUT, only that latest list is sent next. Every successful
 * response updates the confirmed rollback point, but [onCommitted] runs only for a response that was
 * final when received. Synchronization remains active until that callback completes. A failure rolls
 * back and invokes [onError] only when it belongs to the latest intent.
 *
 * [reset] establishes a new session boundary: it cancels the current worker and invalidates any result
 * that may still be returned by a cancellation-uncooperative update function.
 */
class UserProjectMembershipCoordinator(
    private val scope: CoroutineScope,
    initialConfirmed: UserProjects,
    private val update: suspend (List<String>) -> AppResult<UserProjects>,
    private val onStateChanged: (UserProjectMembershipSyncState) -> Unit,
    private val onCommitted: suspend (UserProjects) -> Unit = {},
    private val onError: suspend (ApiError) -> Unit = {},
) {
    private data class Attempt(
        val projects: List<String>,
        val intentVersion: Long,
    )

    private val lock = Any()

    private var generation = 0L
    private var intentVersion = 0L
    private var nextWorkerToken = 0L
    private var activeWorkerToken: Long? = null
    private var workerJob: Job? = null

    private var confirmed = initialConfirmed
    private var desiredProjects = initialConfirmed.projects
    private var currentState = UserProjectMembershipSyncState(
        displayed = initialConfirmed,
        syncing = false,
    )

    val state: UserProjectMembershipSyncState
        get() = synchronized(lock) { currentState }

    init {
        onStateChanged(currentState)
    }

    /**
     * Toggles [project] against the latest desired list, not against the last server response.
     *
     * Removing the final item is valid and produces an empty membership list.
     */
    fun toggle(project: String) {
        var workerToStart: Job? = null
        val stateToEmit = synchronized(lock) {
            desiredProjects = if (project in desiredProjects) {
                desiredProjects.filterNot { it == project }
            } else {
                desiredProjects + project
            }
            intentVersion += 1

            currentState = UserProjectMembershipSyncState(
                displayed = optimisticUserProjects(),
                syncing = true,
            )

            if (activeWorkerToken == null) {
                val token = ++nextWorkerToken
                val workerGeneration = generation
                activeWorkerToken = token
                workerToStart = scope.launch(start = CoroutineStart.LAZY) {
                    synchronize(workerGeneration, token)
                }
                workerJob = workerToStart
            }

            currentState
        }

        // Emit the optimistic selection before allowing an immediately-completing update to emit its result.
        onStateChanged(stateToEmit)
        workerToStart?.start()
    }

    /**
     * Replaces both confirmed and desired state for a new session (or a freshly loaded session).
     */
    fun reset(newConfirmed: UserProjects) {
        val jobToCancel: Job?
        val stateToEmit: UserProjectMembershipSyncState

        synchronized(lock) {
            generation += 1
            intentVersion = 0
            confirmed = newConfirmed
            desiredProjects = newConfirmed.projects

            jobToCancel = workerJob
            workerJob = null
            activeWorkerToken = null

            currentState = UserProjectMembershipSyncState(
                displayed = newConfirmed,
                syncing = false,
            )
            stateToEmit = currentState
        }

        jobToCancel?.cancel()
        onStateChanged(stateToEmit)
    }

    private suspend fun synchronize(
        workerGeneration: Long,
        workerToken: Long,
    ) {
        try {
            while (currentCoroutineContext().isActive) {
                val attempt = synchronized(lock) {
                    if (!isCurrentWorker(workerGeneration, workerToken)) return
                    Attempt(
                        projects = desiredProjects.toList(),
                        intentVersion = intentVersion,
                    )
                }

                val result = try {
                    update(attempt.projects)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (error: Throwable) {
                    AppResult.Failure(ApiError.Unknown(error))
                }

                if (!belongsToGeneration(workerGeneration)) return

                when (result) {
                    is AppResult.Success -> {
                        val wasFinalWhenReceived: Boolean
                        val stateToEmit: UserProjectMembershipSyncState

                        synchronized(lock) {
                            if (!isCurrentWorker(workerGeneration, workerToken)) return

                            confirmed = result.data
                            wasFinalWhenReceived = intentVersion == attempt.intentVersion

                            if (wasFinalWhenReceived) {
                                // The final API response, including activeProject, is authoritative.
                                desiredProjects = result.data.projects
                                currentState = UserProjectMembershipSyncState(
                                    displayed = result.data,
                                    // Derived effects in onCommitted are part of synchronization too.
                                    syncing = true,
                                )
                            } else {
                                currentState = UserProjectMembershipSyncState(
                                    displayed = optimisticUserProjects(),
                                    syncing = true,
                                )
                            }
                            stateToEmit = currentState
                        }

                        onStateChanged(stateToEmit)

                        if (!wasFinalWhenReceived) {
                            // Do not delay a newer user intent with derived work for an obsolete response.
                            continue
                        }

                        if (belongsToGeneration(workerGeneration)) {
                            onCommitted(result.data)
                        }

                        var completedState: UserProjectMembershipSyncState? = null
                        synchronized(lock) {
                            if (!isCurrentWorker(workerGeneration, workerToken)) return

                            if (intentVersion == attempt.intentVersion) {
                                clearWorker(workerToken)
                                currentState = UserProjectMembershipSyncState(
                                    displayed = result.data,
                                    syncing = false,
                                )
                                completedState = currentState
                            }
                        }

                        if (completedState != null) {
                            onStateChanged(requireNotNull(completedState))
                            return
                        }
                        // A toggle arrived while onCommitted was suspended. Preserve its optimistic state
                        // and let this same worker submit it next.
                    }

                    is AppResult.Failure -> {
                        val isFinal: Boolean
                        var stateToEmit: UserProjectMembershipSyncState? = null

                        synchronized(lock) {
                            if (!isCurrentWorker(workerGeneration, workerToken)) return

                            isFinal = intentVersion == attempt.intentVersion
                            if (isFinal) {
                                desiredProjects = confirmed.projects
                                clearWorker(workerToken)
                                currentState = UserProjectMembershipSyncState(
                                    displayed = confirmed,
                                    syncing = false,
                                )
                                stateToEmit = currentState
                            }
                        }

                        if (isFinal) {
                            onStateChanged(requireNotNull(stateToEmit))
                            if (belongsToGeneration(workerGeneration)) {
                                onError(result.error)
                            }
                            return
                        }
                        // The failed request was already obsolete. Loop and submit the latest intent.
                    }
                }
            }
        } finally {
            synchronized(lock) {
                if (belongsToGeneration(workerGeneration) && activeWorkerToken == workerToken) {
                    clearWorker(workerToken)
                }
            }
        }
    }

    private fun optimisticUserProjects(): UserProjects {
        val activeProject = confirmed.activeProject
            .takeIf { it in desiredProjects }
            ?: desiredProjects.firstOrNull().orEmpty()

        return UserProjects(
            projects = desiredProjects,
            activeProject = activeProject,
        )
    }

    private fun belongsToGeneration(workerGeneration: Long): Boolean = synchronized(lock) {
        generation == workerGeneration
    }

    private fun isCurrentWorker(
        workerGeneration: Long,
        workerToken: Long,
    ): Boolean = generation == workerGeneration && activeWorkerToken == workerToken

    private fun clearWorker(workerToken: Long) {
        if (activeWorkerToken == workerToken) {
            activeWorkerToken = null
            workerJob = null
        }
    }
}
