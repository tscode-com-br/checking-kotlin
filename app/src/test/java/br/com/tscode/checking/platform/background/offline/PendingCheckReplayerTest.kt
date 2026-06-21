package br.com.tscode.checking.platform.background.offline

import br.com.tscode.checking.core.error.ApiError
import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.domain.model.ActivityKind
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.HistoryState
import br.com.tscode.checking.domain.model.InformeType
import br.com.tscode.checking.domain.model.LocationMatch
import br.com.tscode.checking.domain.model.LocationOptions
import br.com.tscode.checking.domain.model.MatchStatus
import br.com.tscode.checking.domain.offline.PendingCheckEvent
import br.com.tscode.checking.domain.repository.CheckRepository
import br.com.tscode.checking.platform.activitylog.ActivityLogger
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/**
 * P8 — drains the offline queue by replaying through the LIVE endpoints (no client geometry):
 * Decided submits verbatim; Raw matches the stored position, decides with the shared situation
 * engine, then submits — both with the ORIGINAL capture time + id. Network/expired-session retries
 * (keeps events); HTTP errors drop (no poison). Processed oldest-first.
 */
class PendingCheckReplayerTest {

    private val chave = "HR70"
    private val projeto = "P80"
    private val pending = mutableListOf<PendingCheckEvent>()

    private val queue = mockk<OfflineCheckQueue> {
        coEvery { peekAll() } answers { pending.sortedBy { it.capturedAtEpochMs } }
        coEvery { remove(any()) } answers {
            val id = firstArg<String>()
            pending.removeAll { it.clientEventId == id }
            Unit
        }
        coEvery { size() } answers { pending.size }
    }
    private val checkRepository = mockk<CheckRepository>()
    private val activityLogger = mockk<ActivityLogger>(relaxed = true)
    private val replayer = PendingCheckReplayer(queue, checkRepository, activityLogger)

    private fun raw(id: String, at: Long) = PendingCheckEvent.Raw(chave, projeto, at, id, 1.3, 103.8, 10.0)
    private fun decided(id: String, at: Long, action: String = "checkout", local: String? = "Zona Mista") =
        PendingCheckEvent.Decided(chave, projeto, at, id, action, local, "normal")

    private fun state(last: CheckAction?) = HistoryState(
        found = true, chave = chave, projeto = projeto, currentAction = last, currentLocal = null,
        hasCurrentDayCheckin = last == CheckAction.CHECKIN,
        lastCheckinAt = if (last == CheckAction.CHECKIN) Instant.now() else null,
        lastCheckoutAt = if (last == CheckAction.CHECKOUT) Instant.now() else null,
        transportEnabled = false,
    )

    private fun match(status: MatchStatus, local: String? = null) = LocationMatch(
        matched = status == MatchStatus.MATCHED, resolvedLocal = local, label = local ?: "",
        status = status, message = "", accuracyMeters = 10.0, accuracyThresholdMeters = 50,
        minimumCheckoutDistanceMeters = 2000, nearestWorkplaceDistanceMeters = null,
    )

    private val options = LocationOptions(listOf("Unidade P80"), accuracyThresholdMeters = 50, mixedZoneIntervalMinutes = 15)

    @Test
    fun decided_replays_verbatim_with_original_time_and_id() = runTest {
        pending.add(decided("d", at = 1000, action = "checkout", local = "Zona Mista"))
        coEvery { checkRepository.submit(any(), any(), any(), any(), any(), any(), any()) } returns
            AppResult.Success(state(CheckAction.CHECKOUT))

        assertEquals(PendingCheckReplayer.DrainResult.COMPLETED, replayer.drain())
        assertTrue(pending.isEmpty())
        coVerify {
            checkRepository.submit(
                chave, projeto, CheckAction.CHECKOUT, "Zona Mista", InformeType.NORMAL,
                Instant.ofEpochMilli(1000), "d",
            )
        }
    }

    @Test
    fun raw_matches_decides_and_submits_with_original_time_and_id() = runTest {
        pending.add(raw("r", at = 2000))
        coEvery { checkRepository.matchLocation(any(), any(), any()) } returns
            AppResult.Success(match(MatchStatus.MATCHED, "Unidade P80"))
        coEvery { checkRepository.getState(chave) } returns AppResult.Success(state(CheckAction.CHECKOUT))
        coEvery { checkRepository.getLocations() } returns AppResult.Success(options)
        coEvery { checkRepository.submit(any(), any(), any(), any(), any(), any(), any()) } returns
            AppResult.Success(state(CheckAction.CHECKIN))

        assertEquals(PendingCheckReplayer.DrainResult.COMPLETED, replayer.drain())
        coVerify {
            checkRepository.submit(
                chave, projeto, CheckAction.CHECKIN, "Unidade P80", InformeType.NORMAL,
                Instant.ofEpochMilli(2000), "r",
            )
        }
    }

    @Test
    fun raw_with_no_action_is_consumed_without_submitting() = runTest {
        // NOT_IN_KNOWN_LOCATION after a check-OUT → still no action (engine returns null); consumed.
        pending.add(raw("r", at = 3000))
        coEvery { checkRepository.matchLocation(any(), any(), any()) } returns
            AppResult.Success(match(MatchStatus.NOT_IN_KNOWN_LOCATION))
        coEvery { checkRepository.getState(chave) } returns AppResult.Success(state(CheckAction.CHECKOUT))
        coEvery { checkRepository.getLocations() } returns AppResult.Success(options)

        assertEquals(PendingCheckReplayer.DrainResult.COMPLETED, replayer.drain())
        assertTrue(pending.isEmpty())
        coVerify(exactly = 0) { checkRepository.submit(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun raw_not_in_known_location_after_checkin_replays_unregistered_checkin() = runTest {
        // Change A continuation (P6.2): offline replay must coherently submit the "Localização não
        // Cadastrada" check-in (last action = check-in at a registered location), with the ORIGINAL
        // time + id. (Relies on the Phase-5 backend relaxation for the app client.)
        pending.add(raw("r", at = 3000))
        coEvery { checkRepository.matchLocation(any(), any(), any()) } returns
            AppResult.Success(match(MatchStatus.NOT_IN_KNOWN_LOCATION))
        coEvery { checkRepository.getState(chave) } returns
            AppResult.Success(state(CheckAction.CHECKIN).copy(currentLocal = "Unidade P80"))
        coEvery { checkRepository.getLocations() } returns AppResult.Success(options)
        coEvery { checkRepository.submit(any(), any(), any(), any(), any(), any(), any()) } returns
            AppResult.Success(state(CheckAction.CHECKIN))

        assertEquals(PendingCheckReplayer.DrainResult.COMPLETED, replayer.drain())
        assertTrue(pending.isEmpty())
        coVerify {
            checkRepository.submit(
                chave, projeto, CheckAction.CHECKIN, "Localização não Cadastrada", InformeType.NORMAL,
                Instant.ofEpochMilli(3000), "r",
            )
        }
    }

    @Test
    fun network_failure_retries_and_keeps_event() = runTest {
        pending.add(decided("d", at = 1000))
        coEvery { checkRepository.submit(any(), any(), any(), any(), any(), any(), any()) } returns
            AppResult.Failure(ApiError.Network)
        assertEquals(PendingCheckReplayer.DrainResult.RETRY, replayer.drain())
        assertEquals(1, pending.size)
    }

    @Test
    fun http_4xx_drops_event() = runTest {
        pending.add(decided("d", at = 1000))
        coEvery { checkRepository.submit(any(), any(), any(), any(), any(), any(), any()) } returns
            AppResult.Failure(ApiError.Http(422, "bad local"))
        assertEquals(PendingCheckReplayer.DrainResult.COMPLETED, replayer.drain())
        assertTrue(pending.isEmpty())
    }

    @Test
    fun drain_logs_syncing_count_and_synced_on_success() = runTest {
        // plan004 — the drain logs the queued count, then a synced row for the replayed check-out.
        pending.add(decided("d", at = 1000, action = "checkout", local = "Zona Mista"))
        coEvery { checkRepository.submit(any(), any(), any(), any(), any(), any(), any()) } returns
            AppResult.Success(state(CheckAction.CHECKOUT))

        replayer.drain()

        verify { activityLogger.logSyncing(1) }
        verify { activityLogger.logSynced(ActivityKind.CHECK_OUT, "Zona Mista") }
    }

    @Test
    fun drain_logs_dropped_on_permanent_4xx() = runTest {
        // plan004 — a permanently-dropped queued event (422) is logged as a dropped sync.
        pending.add(decided("d", at = 1000, action = "checkin", local = "Unidade P80"))
        coEvery { checkRepository.submit(any(), any(), any(), any(), any(), any(), any()) } returns
            AppResult.Failure(ApiError.Http(422, "bad local"))

        replayer.drain()

        verify { activityLogger.logSyncDropped(ActivityKind.CHECK_IN) }
    }

    @Test
    fun http_5xx_retries_and_keeps_event() = runTest {
        pending.add(decided("d", at = 1000))
        coEvery { checkRepository.submit(any(), any(), any(), any(), any(), any(), any()) } returns
            AppResult.Failure(ApiError.Http(503, "service unavailable"))
        // Transient server error must NOT lose the event — keep it for a later retry.
        assertEquals(PendingCheckReplayer.DrainResult.RETRY, replayer.drain())
        assertEquals(1, pending.size)
    }

    @Test
    fun drains_in_capture_order_oldest_first() = runTest {
        pending.add(decided("late", at = 2000))
        pending.add(decided("early", at = 1000))
        val order = mutableListOf<String>()
        coEvery { checkRepository.submit(any(), any(), any(), any(), any(), any(), any()) } answers {
            // arg(6) = clientEventId; avoid lastArg() — on a suspend fun it is the Continuation.
            order.add(arg(6))
            AppResult.Success(state(CheckAction.CHECKOUT))
        }
        replayer.drain()
        assertEquals(listOf("early", "late"), order)
    }
}
