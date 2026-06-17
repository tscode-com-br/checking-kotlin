package br.com.tscode.checking.platform.location

import android.util.Log
import br.com.tscode.checking.platform.background.OrchestratorTrigger
import java.time.Instant

data class LocationMeasurement(
    val at: Instant,
    val trigger: OrchestratorTrigger,
    val accuracyMeters: Double,
)

data class LocationMeasurementSummary(
    val count: Int,
    val minMeters: Double,
    val maxMeters: Double,
    val medianMeters: Double,
)

// Debug-only GPS accuracy telemetry (§12.1, T7.1).
// Collects accuracy readings per OrchestratorTrigger and computes median/min/max.
// Only populated for TIMER ticks (where the orchestrator captures GPS directly in
// shouldSkip); GEOFENCE/FOREGROUND captures happen inside the use case and are not
// surfaced here.  Not user-visible — call logSnapshot() to read stats in Logcat.
object LocationMeasurementCollector {

    private const val MAX = 200
    private const val TAG = "LocationTelemetry"
    private val lock = Any()
    private val ring = ArrayDeque<LocationMeasurement>()

    fun record(trigger: OrchestratorTrigger, accuracyMeters: Double) {
        synchronized(lock) {
            ring.addLast(LocationMeasurement(Instant.now(), trigger, accuracyMeters))
            if (ring.size > MAX) ring.removeFirst()
        }
    }

    fun summarize(): Map<OrchestratorTrigger, LocationMeasurementSummary> {
        synchronized(lock) {
            return ring.groupBy { it.trigger }.mapValues { (_, entries) ->
                val sorted = entries.map { it.accuracyMeters }.sorted()
                val mid = sorted.size / 2
                LocationMeasurementSummary(
                    count = sorted.size,
                    minMeters = sorted.first(),
                    maxMeters = sorted.last(),
                    medianMeters = if (sorted.size % 2 == 0) {
                        (sorted[mid - 1] + sorted[mid]) / 2.0
                    } else {
                        sorted[mid]
                    },
                )
            }
        }
    }

    fun snapshot(): List<LocationMeasurement> {
        synchronized(lock) { return ring.toList().asReversed() }
    }

    fun isEmpty(): Boolean {
        synchronized(lock) { return ring.isEmpty() }
    }

    // Dumps per-trigger stats to Logcat (debug console snapshot).
    fun logSnapshot() {
        val summary = summarize()
        if (summary.isEmpty()) {
            Log.d(TAG, "No location measurements recorded yet.")
            return
        }
        summary.forEach { (trigger, s) ->
            Log.d(
                TAG,
                "$trigger  n=${s.count}  " +
                    "min=${"%.1f".format(s.minMeters)}m  " +
                    "median=${"%.1f".format(s.medianMeters)}m  " +
                    "max=${"%.1f".format(s.maxMeters)}m",
            )
        }
    }
}
