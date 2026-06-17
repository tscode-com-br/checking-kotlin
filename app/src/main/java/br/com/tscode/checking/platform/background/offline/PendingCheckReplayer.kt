package br.com.tscode.checking.platform.background.offline

import br.com.tscode.checking.core.error.ApiError
import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.domain.checkrules.resolveAutomaticActivityForMatch
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.InformeType
import br.com.tscode.checking.domain.offline.PendingCheckEvent
import br.com.tscode.checking.domain.repository.CheckRepository
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
) {
    enum class DrainResult { COMPLETED, RETRY }

    private enum class Outcome { DONE, DROP, RETRY }

    suspend fun drain(): DrainResult {
        // Loop so events enqueued DURING a pass are caught in the same run.
        var pass = 0
        while (pass < MAX_PASSES) {
            val pending = queue.peekAll()
            if (pending.isEmpty()) return DrainResult.COMPLETED
            for (event in pending) {
                when (replay(event)) {
                    Outcome.DONE, Outcome.DROP -> queue.remove(event.clientEventId)
                    Outcome.RETRY -> return DrainResult.RETRY // offline / session expired — later
                }
            }
            pass++
        }
        return if (queue.size() == 0) DrainResult.COMPLETED else DrainResult.RETRY
    }

    private suspend fun replay(event: PendingCheckEvent): Outcome = when (event) {
        is PendingCheckEvent.Decided -> replayDecided(event)
        is PendingCheckEvent.Raw -> replayRaw(event)
    }

    private suspend fun replayDecided(e: PendingCheckEvent.Decided): Outcome {
        val action = if (e.action == "checkout") CheckAction.CHECKOUT else CheckAction.CHECKIN
        val informe = if (e.informe == "retroativo") InformeType.RETROATIVO else InformeType.NORMAL
        return outcomeOf(
            checkRepository.submit(
                chave = e.chave,
                projeto = e.projeto,
                action = action,
                local = e.local,
                informe = informe,
                eventTime = Instant.ofEpochMilli(e.capturedAtEpochMs),
                clientEventId = e.clientEventId,
            ),
        )
    }

    private suspend fun replayRaw(e: PendingCheckEvent.Raw): Outcome {
        val match = when (val r = checkRepository.matchLocation(e.latitude, e.longitude, e.accuracyMeters)) {
            is AppResult.Success -> r.data
            is AppResult.Failure -> return failureOutcome(r.error)
        }
        val state = when (val r = checkRepository.getState(e.chave)) {
            is AppResult.Success -> r.data
            is AppResult.Failure -> return failureOutcome(r.error)
        }
        val options = when (val r = checkRepository.getLocations()) {
            is AppResult.Success -> r.data
            is AppResult.Failure -> return failureOutcome(r.error)
        }
        val activity = resolveAutomaticActivityForMatch(match, state, options.mixedZoneIntervalMinutes)
            ?: return Outcome.DONE // no action for this reading — consume it
        return outcomeOf(
            checkRepository.submit(
                chave = e.chave,
                projeto = e.projeto,
                action = activity.action,
                local = activity.local,
                informe = InformeType.NORMAL,
                eventTime = Instant.ofEpochMilli(e.capturedAtEpochMs),
                clientEventId = e.clientEventId,
            ),
        )
    }

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

    companion object {
        private const val MAX_PASSES = 5
    }
}
