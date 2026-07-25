package br.com.tscode.checking.platform.background.offline

import br.com.tscode.checking.core.error.ApiError
import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.domain.checkrules.resolveAutomaticActivityForMatch
import br.com.tscode.checking.domain.model.ActivityKind
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.InformeType
import br.com.tscode.checking.domain.offline.PendingCheckEvent
import br.com.tscode.checking.domain.repository.CheckRepository
import br.com.tscode.checking.platform.activitylog.ActivityLogger
import br.com.tscode.checking.platform.background.BackgroundCheckOrchestrator
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

// Drains the offline check queue (P8). Extracted from SyncPendingChecksWorker so the replay logic
// is plain-JVM unit testable (no CoroutineWorker / WorkManager). Replays in capture order:
//  - Decided → submit verbatim (original timestamp + id).
//  - Raw     → POST /check/location with the STORED position → GET /check/state → the SAME engine
//              the live flow uses (resolveAutomaticActivityForMatch) → submit if it decides an
//              action, else consume the reading. The server stays the single matcher; submit dedups
//              by client_event_id, so each real-world event lands exactly once.
@Singleton
class PendingCheckReplayer @Inject constructor(
    private val queue: OfflineCheckQueue,
    private val checkRepository: CheckRepository,
    private val activityLogger: ActivityLogger,
    private val orchestrator: BackgroundCheckOrchestrator,
) {
    enum class DrainResult { COMPLETED, RETRY }

    private enum class Outcome { DONE, DROP, RETRY }

    private data class Confirmation(
        val chave: String,
        val project: String,
        val state: br.com.tscode.checking.domain.model.HistoryState,
    )

    private data class ReplayResult(
        val outcome: Outcome,
        val confirmation: Confirmation? = null,
    )

    suspend fun drain(): DrainResult {
        // Loop so events enqueued DURING a pass are caught in the same run.
        var pass = 0
        var finalConfirmation: Confirmation? = null
        while (pass < MAX_PASSES) {
            val pending = queue.peekAll()
            if (pending.isEmpty()) {
                finalConfirmation?.let {
                    orchestrator.onServerConfirmedState(it.chave, it.project, it.state)
                }
                return DrainResult.COMPLETED
            }
            // Newest queued activity — the reference for the 24h FORMS window. Stable across passes
            // (peekAll is capture-ordered and the newest event drains last), so every event is compared
            // against the same anchor. Only the client can compute this: it holds the whole backlog.
            val newestCapturedAtMs = pending.maxOf { it.capturedAtEpochMs }
            activityLogger.logSyncing(pending.size) // plan004 — replay drain started
            for (event in pending) {
                val replay = replay(event, newestCapturedAtMs)
                if (replay.confirmation != null) finalConfirmation = replay.confirmation
                when (replay.outcome) {
                    Outcome.DONE, Outcome.DROP -> queue.remove(event.clientEventId)
                    // Never arm grace from an intermediate checkout when a later backlog item may
                    // restore CHECKIN. Only a fully completed drain publishes finalConfirmation.
                    Outcome.RETRY -> return DrainResult.RETRY
                }
            }
            pass++
        }
        return if (queue.size() == 0) {
            finalConfirmation?.let {
                orchestrator.onServerConfirmedState(it.chave, it.project, it.state)
            }
            DrainResult.COMPLETED
        } else {
            DrainResult.RETRY
        }
    }

    private suspend fun replay(event: PendingCheckEvent, newestCapturedAtMs: Long): ReplayResult = when (event) {
        is PendingCheckEvent.Decided -> replayDecided(event, newestCapturedAtMs)
        is PendingCheckEvent.Raw -> replayRaw(event, newestCapturedAtMs)
    }

    private suspend fun replayDecided(e: PendingCheckEvent.Decided, newestCapturedAtMs: Long): ReplayResult {
        val action = if (e.action == "checkout") CheckAction.CHECKOUT else CheckAction.CHECKIN
        val informe = if (e.informe == "retroativo") InformeType.RETROATIVO else InformeType.NORMAL
        val submit = checkRepository.submit(
                chave = e.chave,
                projeto = e.projeto,
                action = action,
                local = e.local,
                informe = informe,
                eventTime = Instant.ofEpochMilli(e.capturedAtEpochMs),
                clientEventId = e.clientEventId,
                fillForms = fillFormsFor(e.capturedAtEpochMs, newestCapturedAtMs),
            )
        val outcome = outcomeOf(submit)
        logReplayOutcome(outcome, action, e.local) // plan004
        return ReplayResult(
            outcome = outcome,
            confirmation = (submit as? AppResult.Success)?.data?.let {
                Confirmation(e.chave, e.projeto, it)
            },
        )
    }

    private suspend fun replayRaw(e: PendingCheckEvent.Raw, newestCapturedAtMs: Long): ReplayResult {
        val match = when (val r = checkRepository.matchLocation(e.latitude, e.longitude, e.accuracyMeters)) {
            is AppResult.Success -> r.data
            is AppResult.Failure -> return ReplayResult(failureOutcome(r.error))
        }
        val state = when (val r = checkRepository.getState(e.chave)) {
            is AppResult.Success -> r.data
            is AppResult.Failure -> return ReplayResult(failureOutcome(r.error))
        }
        val options = when (val r = checkRepository.getLocations()) {
            is AppResult.Success -> r.data
            is AppResult.Failure -> return ReplayResult(failureOutcome(r.error))
        }
        val activity = resolveAutomaticActivityForMatch(match, state, options.mixedZoneIntervalMinutes)
            ?: return ReplayResult(Outcome.DONE) // no action for this reading — consume it
        val submit = checkRepository.submit(
                chave = e.chave,
                projeto = e.projeto,
                action = activity.action,
                local = activity.local,
                informe = InformeType.NORMAL,
                eventTime = Instant.ofEpochMilli(e.capturedAtEpochMs),
                clientEventId = e.clientEventId,
                fillForms = fillFormsFor(e.capturedAtEpochMs, newestCapturedAtMs),
            )
        val outcome = outcomeOf(submit)
        logReplayOutcome(outcome, activity.action, activity.local) // plan004
        return ReplayResult(
            outcome = outcome,
            confirmation = (submit as? AppResult.Success)?.data?.let {
                Confirmation(e.chave, e.projeto, it)
            },
        )
    }

    // A replayed event fills FORMS only if it is within 24h of the NEWEST queued activity, so a device
    // offline for several days fills FORMS with just the most recent check-in/out. Older events are still
    // recorded server-side at their real time (sync event + history) — they just don't (re-)fill FORMS.
    // The window is anchored to the backlog's newest activity (not wall-clock now), so a delayed reconnect
    // still fills FORMS for the last day of real activity. Server enforces the same via fill_forms.
    private fun fillFormsFor(capturedAtEpochMs: Long, newestCapturedAtMs: Long): Boolean =
        (newestCapturedAtMs - capturedAtEpochMs) <= FORMS_RECENCY_WINDOW_MS

    private fun outcomeOf(result: AppResult<*>): Outcome = when (result) {
        is AppResult.Success -> Outcome.DONE
        is AppResult.Failure -> failureOutcome(result.error)
    }

    // Transient → RETRY later (kept in the queue): network loss, expired session (the accident
    // watcher's silent re-login refreshes the cookie every 15 min), and HTTP 5xx (server overloaded
    // or mid-deploy — must NOT lose a real check-in over a server hiccup).
    // Permanent → DROP (so one bad payload can't block the queue forever): HTTP 4xx (e.g. a 422 from
    // an invalid local), and Conflict/Unknown (a code/data bug that retrying won't fix).
    private fun failureOutcome(error: ApiError): Outcome = when (error) {
        ApiError.Network, ApiError.Unauthorized -> Outcome.RETRY
        is ApiError.Http -> if (error.status >= 500) Outcome.RETRY else Outcome.DROP
        else -> Outcome.DROP
    }

    // plan004 — side-effect-only Activities log for a replayed event's terminal outcome (RETRY keeps it
    // queued, so nothing is logged then). Crash-proof via the logger; never alters the drain result.
    private fun logReplayOutcome(outcome: Outcome, action: CheckAction, local: String?) {
        val kind = if (action == CheckAction.CHECKOUT) ActivityKind.CHECK_OUT else ActivityKind.CHECK_IN
        when (outcome) {
            Outcome.DONE -> activityLogger.logSynced(kind, local)
            Outcome.DROP -> activityLogger.logSyncDropped(kind)
            Outcome.RETRY -> {}
        }
    }

    companion object {
        private const val MAX_PASSES = 5
        private const val FORMS_RECENCY_WINDOW_MS = 24L * 60 * 60 * 1000 // 24h
    }
}
