package br.com.tscode.checking.platform.background

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal const val LOW_ACCURACY_RETRY_INTERVAL_MILLIS = 180_000L

/**
 * Identifies the single low-accuracy episode that may be active at a time.
 *
 * Both values deliberately participate in equality: changing either the authenticated session or
 * the active project replaces the previous episode and its pending retry.
 */
internal data class LowAccuracyEpisodeKey(
    val chave: String,
    val project: String,
)

/**
 * Owns the retry loop for a low-location-accuracy episode.
 *
 * The supplied [scope] defines the component lifetime. This class has no Android or dependency
 * injection concerns so callers can use a process scope in production and a virtual-time scope in
 * tests.
 */
internal class LowAccuracyRetryScheduler(
    private val scope: CoroutineScope,
    private val retryDelayMillis: Long = LOW_ACCURACY_RETRY_INTERVAL_MILLIS,
) {
    private data class Episode(
        val generation: Long,
        val key: LowAccuracyEpisodeKey,
        val job: Job,
    )

    private val monitor = Any()
    private var generation = 0L
    private var episode: Episode? = null

    init {
        require(retryDelayMillis > 0L) {
            "retryDelayMillis must be greater than zero"
        }
    }

    /**
     * Starts an episode and returns `true`, or preserves the existing episode for [key] and returns
     * `false`.
     *
     * Preserving an episode also preserves its current delay and callback. A different key cancels
     * the previous job and starts a fresh delay. Retries never overlap: the next delay starts only
     * after [retry] returns.
     */
    fun startOrKeep(
        key: LowAccuracyEpisodeKey,
        retry: suspend (LowAccuracyEpisodeKey) -> Unit,
    ): Boolean {
        lateinit var replacement: Job
        var previous: Job? = null
        val currentGeneration: Long

        synchronized(monitor) {
            if (episode?.key == key) return false

            previous = episode?.job
            currentGeneration = ++generation
            replacement = scope.launch(start = CoroutineStart.LAZY) {
                try {
                    while (
                        currentCoroutineContext().isActive &&
                        isCurrent(key, currentGeneration)
                    ) {
                        delay(retryDelayMillis)
                        if (!isCurrent(key, currentGeneration)) return@launch
                        retry(key)
                    }
                } finally {
                    clearIfCurrent(key, currentGeneration)
                }
            }
            episode = Episode(
                generation = currentGeneration,
                key = key,
                job = replacement,
            )
        }

        previous?.cancel()
        replacement.start()
        return true
    }

    fun isActiveFor(key: LowAccuracyEpisodeKey): Boolean =
        synchronized(monitor) {
            episode?.key == key
        }

    fun activeKey(): LowAccuracyEpisodeKey? =
        synchronized(monitor) {
            episode?.key
        }

    /**
     * Cancels only when [key] is active, or cancels any active episode when [key] is `null`.
     */
    fun cancel(key: LowAccuracyEpisodeKey? = null): Boolean =
        cancelWhen { active -> key == null || active == key }

    /**
     * Cancels the active episode when it no longer belongs to [key].
     *
     * Passing `null` means there is no valid current key, so any episode is cancelled.
     */
    fun cancelUnless(key: LowAccuracyEpisodeKey?): Boolean =
        cancelWhen { active -> active != key }

    private fun cancelWhen(predicate: (LowAccuracyEpisodeKey) -> Boolean): Boolean {
        val job = synchronized(monitor) {
            val current = episode ?: return false
            if (!predicate(current.key)) return false

            episode = null
            generation++
            current.job
        }
        job.cancel()
        return true
    }

    private fun isCurrent(
        key: LowAccuracyEpisodeKey,
        expectedGeneration: Long,
    ): Boolean =
        synchronized(monitor) {
            episode?.let {
                it.key == key && it.generation == expectedGeneration
            } == true
        }

    private fun clearIfCurrent(
        key: LowAccuracyEpisodeKey,
        expectedGeneration: Long,
    ) {
        synchronized(monitor) {
            val current = episode
            if (current?.key == key && current.generation == expectedGeneration) {
                episode = null
            }
        }
    }
}
