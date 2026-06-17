package br.com.tscode.checking.platform.background.offline

import android.content.Context
import br.com.tscode.checking.data.local.AppPreferencesDataSource
import br.com.tscode.checking.domain.offline.PendingCheckEvent
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * P8 — the persistent offline queue: capture-ordered, deduped by id, capped, and surviving a
 * "process restart" (a fresh queue over the same backing store). The worker scheduling side effect
 * is stubbed so this stays a pure-JVM test.
 */
class OfflineCheckQueueTest {

    // In-memory stand-in for the DataStore-backed pendingChecksJson preference.
    private val store = MutableStateFlow("")
    private val appPrefs = mockk<AppPreferencesDataSource> {
        every { pendingChecksJson } returns store
        // setPendingChecksJson returns Preferences (DataStore.edit), not Unit → return a relaxed mock.
        coEvery { setPendingChecksJson(any()) } answers { store.value = firstArg(); mockk(relaxed = true) }
    }
    private val context = mockk<Context>(relaxed = true)
    private val queue = OfflineCheckQueue(context, appPrefs)

    @Before
    fun setup() {
        mockkObject(SyncPendingChecksWorker.Companion)
        every { SyncPendingChecksWorker.enqueue(any()) } just Runs
    }

    @After
    fun teardown() {
        unmockkObject(SyncPendingChecksWorker.Companion)
    }

    private fun raw(id: String, at: Long, lat: Double = 1.0) =
        PendingCheckEvent.Raw("HR70", "P80", at, id, lat, 103.0, 10.0)

    private fun decided(id: String, at: Long) =
        PendingCheckEvent.Decided("HR70", "P80", at, id, "checkout", "Zona Mista", "normal")

    @Test
    fun enqueue_then_peek_returns_in_capture_order() = runTest {
        queue.enqueue(raw("b", at = 200))
        queue.enqueue(raw("a", at = 100))
        assertEquals(listOf("a", "b"), queue.peekAll().map { it.clientEventId })
    }

    @Test
    fun enqueue_same_id_replaces_instead_of_duplicating() = runTest {
        queue.enqueue(decided("x", at = 100))
        queue.enqueue(decided("x", at = 150))
        assertEquals(1, queue.size())
        assertEquals(150L, queue.peekAll().single().capturedAtEpochMs)
    }

    @Test
    fun remove_drops_only_that_id() = runTest {
        queue.enqueue(raw("a", at = 100))
        queue.enqueue(decided("b", at = 200))
        queue.remove("a")
        assertEquals(listOf("b"), queue.peekAll().map { it.clientEventId })
    }

    @Test
    fun survives_serialization_roundtrip_for_both_variants() = runTest {
        queue.enqueue(raw("r", at = 100, lat = 1.2345))
        queue.enqueue(decided("d", at = 200))
        // A fresh queue over the same store reads them back (simulates a process restart).
        val reopened = OfflineCheckQueue(context, appPrefs).peekAll()
        assertEquals(2, reopened.size)
        val r = reopened.first { it.clientEventId == "r" } as PendingCheckEvent.Raw
        assertEquals(1.2345, r.latitude, 0.0)
        val d = reopened.first { it.clientEventId == "d" } as PendingCheckEvent.Decided
        assertEquals("checkout", d.action)
        assertEquals("Zona Mista", d.local)
    }

    @Test
    fun caps_queue_dropping_oldest() = runTest {
        for (i in 1..205) queue.enqueue(raw(id = "e$i", at = i.toLong()))
        val all = queue.peekAll()
        assertEquals(200, all.size)
        // 1..5 (oldest) were dropped; e6 is now the oldest kept.
        assertEquals("e6", all.first().clientEventId)
        assertEquals(6L, all.first().capturedAtEpochMs)
    }
}
