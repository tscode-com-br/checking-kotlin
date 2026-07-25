package br.com.tscode.checking.platform.background

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LowAccuracyRetrySchedulerTest {
    private val firstKey =
        LowAccuracyEpisodeKey(
            chave = "session-a",
            project = "project-1",
        )
    private val secondKey =
        LowAccuracyEpisodeKey(
            chave = "session-a",
            project = "project-2",
        )

    @Test
    fun `retries every 180 seconds while episode remains active`() =
        runTest {
            val retryTimes = mutableListOf<Long>()
            val scheduler = LowAccuracyRetryScheduler(this)

            assertTrue(
                scheduler.startOrKeep(firstKey) {
                    retryTimes += testScheduler.currentTime
                },
            )
            runCurrent()

            advanceTimeBy(179_999L)
            runCurrent()
            assertEquals(emptyList<Long>(), retryTimes)

            advanceTimeBy(1L)
            runCurrent()
            assertEquals(listOf(180_000L), retryTimes)

            advanceTimeBy(180_000L)
            runCurrent()
            assertEquals(listOf(180_000L, 360_000L), retryTimes)
            assertTrue(scheduler.isActiveFor(firstKey))

            scheduler.cancel()
            runCurrent()
        }

    @Test
    fun `same key preserves elapsed delay and original callback`() =
        runTest {
            val callbacks = mutableListOf<String>()
            val scheduler = LowAccuracyRetryScheduler(this)

            assertTrue(scheduler.startOrKeep(firstKey) { callbacks += "original" })
            runCurrent()
            advanceTimeBy(120_000L)
            runCurrent()

            assertFalse(scheduler.startOrKeep(firstKey) { callbacks += "replacement" })
            assertEquals(firstKey, scheduler.activeKey())

            advanceTimeBy(59_999L)
            runCurrent()
            assertEquals(emptyList<String>(), callbacks)

            advanceTimeBy(1L)
            runCurrent()
            assertEquals(listOf("original"), callbacks)

            scheduler.cancel()
            runCurrent()
        }

    @Test
    fun `different key cancels old job and starts a fresh delay`() =
        runTest {
            val callbacks = mutableListOf<Pair<Long, LowAccuracyEpisodeKey>>()
            val scheduler = LowAccuracyRetryScheduler(this)

            assertTrue(
                scheduler.startOrKeep(firstKey) {
                    callbacks += testScheduler.currentTime to it
                },
            )
            runCurrent()
            advanceTimeBy(120_000L)
            runCurrent()

            assertTrue(
                scheduler.startOrKeep(secondKey) {
                    callbacks += testScheduler.currentTime to it
                },
            )
            runCurrent()
            assertEquals(secondKey, scheduler.activeKey())

            advanceTimeBy(179_999L)
            runCurrent()
            assertEquals(emptyList<Pair<Long, LowAccuracyEpisodeKey>>(), callbacks)

            advanceTimeBy(1L)
            runCurrent()
            assertEquals(listOf(300_000L to secondKey), callbacks)

            scheduler.cancel()
            runCurrent()
        }

    @Test
    fun `cancel by key or current-key mismatch prevents pending retries`() =
        runTest {
            var retries = 0
            val scheduler = LowAccuracyRetryScheduler(this)

            scheduler.startOrKeep(firstKey) { retries++ }
            runCurrent()

            assertFalse(scheduler.cancel(secondKey))
            assertTrue(scheduler.isActiveFor(firstKey))
            assertFalse(scheduler.cancelUnless(firstKey))
            assertTrue(scheduler.cancelUnless(secondKey))
            assertNull(scheduler.activeKey())

            advanceTimeBy(LOW_ACCURACY_RETRY_INTERVAL_MILLIS * 2)
            runCurrent()
            assertEquals(0, retries)
        }

    @Test
    fun `suspended callback completes before next delay begins`() =
        runTest {
            val releaseFirstRetry = CompletableDeferred<Unit>()
            val retryTimes = mutableListOf<Long>()
            val scheduler = LowAccuracyRetryScheduler(this)

            scheduler.startOrKeep(firstKey) {
                retryTimes += testScheduler.currentTime
                if (retryTimes.size == 1) releaseFirstRetry.await()
            }
            runCurrent()

            advanceTimeBy(180_000L)
            runCurrent()
            assertEquals(listOf(180_000L), retryTimes)

            advanceTimeBy(180_000L)
            runCurrent()
            assertEquals(
                "a suspended callback must not create an overlapping retry",
                listOf(180_000L),
                retryTimes,
            )

            releaseFirstRetry.complete(Unit)
            runCurrent()
            advanceTimeBy(179_999L)
            runCurrent()
            assertEquals(listOf(180_000L), retryTimes)

            advanceTimeBy(1L)
            runCurrent()
            assertEquals(listOf(180_000L, 540_000L), retryTimes)

            scheduler.cancel()
            runCurrent()
        }

    @Test
    fun `cancelling episode cancels a suspended callback and ends loop`() =
        runTest {
            val callbackStarted = CompletableDeferred<Unit>()
            val callbackCancelled = CompletableDeferred<Unit>()
            var retries = 0
            val scheduler = LowAccuracyRetryScheduler(this)

            scheduler.startOrKeep(firstKey) {
                retries++
                callbackStarted.complete(Unit)
                try {
                    awaitCancellation()
                } finally {
                    callbackCancelled.complete(Unit)
                }
            }
            runCurrent()

            advanceTimeBy(180_000L)
            runCurrent()
            assertTrue(callbackStarted.isCompleted)
            assertEquals(1, retries)

            assertTrue(scheduler.cancel(firstKey))
            assertNull(scheduler.activeKey())
            runCurrent()

            assertTrue(callbackCancelled.isCompleted)
            advanceTimeBy(LOW_ACCURACY_RETRY_INTERVAL_MILLIS * 2)
            runCurrent()
            assertEquals(1, retries)
        }
}
