package br.com.tscode.checking.platform.background.diagnostics

import br.com.tscode.checking.platform.background.OrchestratorTrigger
import java.time.Instant

enum class EvaluationOutcome {
    SUBMITTED,      // action was performed (check-in or check-out)
    NO_ACTION,      // situation matched current state — nothing to do
    SKIP,           // device hasn't moved beyond threshold (TIMER ticks only)
    PAUSED,         // scheduled pause window active
    NETWORK_ERROR,  // server/network failure during the run
    TOGGLE_OFF,     // automatic activities were disabled at run time
}

data class EvaluationEntry(
    val at: Instant,
    val trigger: OrchestratorTrigger,
    val accuracyMeters: Double?,  // GPS fix accuracy; null if not captured this run
    val resolvedLocal: String?,   // matched location name; null if no match or no action
    val decidedAction: String?,   // "CHECKIN" / "CHECKOUT"; null if no action taken
    val outcome: EvaluationOutcome,
)

// Thread-safe in-memory ring buffer of the last MAX orchestrator evaluations (§23.10, T3B.11).
// A plain singleton so it's accessible from the orchestrator, the watchdog, and the debug UI
// without going through the DI graph.
object EvaluationLog {
    private const val MAX = 50
    private val ring = ArrayDeque<EvaluationEntry>()

    @Synchronized
    fun record(entry: EvaluationEntry) {
        ring.addLast(entry)
        if (ring.size > MAX) ring.removeFirst()
    }

    // Returns a snapshot newest-first.
    @Synchronized
    fun snapshot(): List<EvaluationEntry> = ring.toList().asReversed()

    @Synchronized
    fun isEmpty(): Boolean = ring.isEmpty()
}
