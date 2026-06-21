package br.com.tscode.checking.domain.usecase

import br.com.tscode.checking.core.error.ApiError
import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.core.time.Clock
import br.com.tscode.checking.domain.checkrules.resolveAutomaticActivityForMatch
import br.com.tscode.checking.domain.model.ActivityActor
import br.com.tscode.checking.domain.model.ActivityKind
import br.com.tscode.checking.domain.model.ActivitySeverity
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.HistoryState
import br.com.tscode.checking.domain.model.InformeType
import br.com.tscode.checking.domain.model.UserProjects
import br.com.tscode.checking.domain.offline.PendingCheckEvent
import br.com.tscode.checking.domain.repository.CheckRepository
import br.com.tscode.checking.platform.activitylog.ActivityLogger
import br.com.tscode.checking.platform.background.offline.OfflineCheckQueue
import java.util.UUID
import javax.inject.Inject

sealed class AutoActivitiesResult {
    data class Submitted(val action: CheckAction, val local: String?, val newState: HistoryState) : AutoActivitiesResult()
    object NoAction : AutoActivitiesResult()
    object NetworkError : AutoActivitiesResult()
    object NotConfigured : AutoActivitiesResult()
}

class RunAutomaticActivitiesUseCase @Inject constructor(
    private val captureLocationUseCase: CaptureLocationUseCase,
    private val checkRepository: CheckRepository,
    private val offlineQueue: OfflineCheckQueue,
    private val clock: Clock,
    private val activityLogger: ActivityLogger,
) {
    suspend operator fun invoke(
        chave: String,
        userProjects: UserProjects?,
        currentState: HistoryState?,
        mixedZoneIntervalMinutes: Int,
        accuracyThresholdMeters: Int,
    ): AutoActivitiesResult {
        val projeto = userProjects?.activeProject?.takeIf { it.isNotEmpty() }
            ?: run {
                activityLogger.logSystem("No active project — skipped.", ActivitySeverity.WARNING) // plan004
                return AutoActivitiesResult.NotConfigured
            }

        val locationResult = captureLocationUseCase(accuracyThresholdMeters)
        val match = when (locationResult) {
            is LocationCaptureResult.Matched -> locationResult.match
            is LocationCaptureResult.NetworkError -> {
                // Offline (server unreachable) but a GPS fix was obtained → queue the raw reading.
                // The server matches + decides it on reconnect (SyncPendingChecksWorker), recorded
                // at the real capture time. No client-side geometry needed (P8).
                locationResult.reading?.let { reading ->
                    offlineQueue.enqueue(
                        PendingCheckEvent.Raw(
                            chave = chave,
                            projeto = projeto,
                            capturedAtEpochMs = clock.now().toEpochMilli(),
                            clientEventId = UUID.randomUUID().toString(),
                            latitude = reading.lat,
                            longitude = reading.lon,
                            accuracyMeters = reading.accuracyMeters,
                        ),
                    )
                    // plan004 — server unreachable during capture; the raw GPS reading was queued to be
                    // matched + decided on reconnect (no action kind yet → logged as a LOCATION event).
                    activityLogger.logLocation(
                        "Location reading queued offline — will sync on reconnect.",
                        null,
                        ActivitySeverity.WARNING,
                    )
                }
                return AutoActivitiesResult.NetworkError
            }
            else -> return AutoActivitiesResult.NoAction
        }

        val activity = resolveAutomaticActivityForMatch(match, currentState, mixedZoneIntervalMinutes)
            ?: return AutoActivitiesResult.NoAction

        // Pre-generate id + timestamp so a failed submit can be queued with the SAME identity
        // (exactly-once: if the submit actually reached the server, the replay dedups by id).
        val clientEventId = UUID.randomUUID().toString()
        val eventTime = clock.now()
        return when (
            val r = checkRepository.submit(
                chave = chave,
                projeto = projeto,
                action = activity.action,
                local = activity.local,
                informe = InformeType.NORMAL,
                eventTime = eventTime,
                clientEventId = clientEventId,
            )
        ) {
            is AppResult.Success -> {
                // plan004 — automatic check-in/out succeeded (actor=SYS). Side-effect-only log.
                if (activity.action == CheckAction.CHECKIN) {
                    activityLogger.logCheckIn(ActivityActor.SYS, activity.local, success = true)
                } else {
                    activityLogger.logCheckOut(ActivityActor.SYS, activity.local, success = true)
                }
                AutoActivitiesResult.Submitted(activity.action, activity.local, r.data)
            }
            is AppResult.Failure -> {
                if (r.error is ApiError.Network) {
                    offlineQueue.enqueue(
                        PendingCheckEvent.Decided(
                            chave = chave,
                            projeto = projeto,
                            capturedAtEpochMs = eventTime.toEpochMilli(),
                            clientEventId = clientEventId,
                            action = if (activity.action == CheckAction.CHECKOUT) "checkout" else "checkin",
                            local = activity.local,
                            informe = "normal",
                        ),
                    )
                    // plan004 — queued offline (actor=SYS).
                    activityLogger.logQueuedOffline(
                        ActivityActor.SYS,
                        if (activity.action == CheckAction.CHECKIN) ActivityKind.CHECK_IN else ActivityKind.CHECK_OUT,
                        activity.local,
                    )
                } else {
                    // plan004 — automatic check-in/out failed (non-network; actor=SYS).
                    if (activity.action == CheckAction.CHECKIN) {
                        activityLogger.logCheckIn(ActivityActor.SYS, activity.local, success = false)
                    } else {
                        activityLogger.logCheckOut(ActivityActor.SYS, activity.local, success = false)
                    }
                }
                AutoActivitiesResult.NetworkError
            }
        }
    }
}
