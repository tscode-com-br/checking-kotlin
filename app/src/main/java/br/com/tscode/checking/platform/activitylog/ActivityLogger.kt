package br.com.tscode.checking.platform.activitylog

import br.com.tscode.checking.core.time.Clock
import br.com.tscode.checking.data.local.activitylog.ActivityLog
import br.com.tscode.checking.domain.model.ActivityActor
import br.com.tscode.checking.domain.model.ActivityKind
import br.com.tscode.checking.domain.model.ActivityLogEntry
import br.com.tscode.checking.domain.model.ActivitySeverity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * plan004 §3.3 — typed, CRASH-PROOF façade for the Activities log. Every helper builds the exact English
 * description + picks kind/severity/actor and persists OFF the caller's thread on [appScope]. The whole
 * body is `runCatching`-wrapped (twice): logging is best-effort diagnostics and must NEVER throw into a
 * check-in / FGS / receiver path (golden rule 2). Descriptions are English-only by design.
 */
@Singleton
class ActivityLogger @Inject constructor(
    private val clock: Clock,
    private val activityLog: ActivityLog,
    @ApplicationScope private val appScope: CoroutineScope,
) {
    /** High-frequency/low-signal helpers ([logTrigger]) are muted when false. Default on. */
    var verbose: Boolean = true

    // ── Check-in / check-out (required exact descriptions, plan004 §3.1) ──────────────────────────
    fun logCheckIn(actor: ActivityActor, location: String?, success: Boolean) = log(
        actor, ActivityKind.CHECK_IN,
        if (success) ActivitySeverity.SUCCESS else ActivitySeverity.FAILURE,
        (if (success) "Check-in at " else "Check-in failed at ") + locText(location) + ".",
        location,
    )

    fun logCheckOut(actor: ActivityActor, location: String?, success: Boolean) = log(
        actor, ActivityKind.CHECK_OUT,
        if (success) ActivitySeverity.SUCCESS else ActivitySeverity.FAILURE,
        (if (success) "Check-out at " else "Check-out failed at ") + locText(location) + ".",
        location,
    )

    // ── Active / inactive (required exact base, plan004 §3.1; optional detail in parentheses) ─────
    fun logActive(detail: String? = null) =
        log(ActivityActor.SYS, ActivityKind.ACTIVE, ActivitySeverity.INFO, "Checking is now active." + detailSuffix(detail), null)

    fun logInactive(detail: String? = null) =
        log(ActivityActor.SYS, ActivityKind.INACTIVE, ActivitySeverity.INFO, "Checking is now inactive." + detailSuffix(detail), null)

    // ── Offline queue / sync ─────────────────────────────────────────────────────────────────────
    fun logQueuedOffline(actor: ActivityActor, kind: ActivityKind, location: String?) =
        log(actor, ActivityKind.SYNC, ActivitySeverity.WARNING, actText(kind) + " queued (offline) at " + locText(location) + ".", location)

    fun logSyncing(count: Int) =
        log(ActivityActor.SYS, ActivityKind.SYNC, ActivitySeverity.INFO, "Syncing $count queued event(s).", null)

    fun logSynced(kind: ActivityKind, location: String?) =
        log(ActivityActor.SYS, ActivityKind.SYNC, ActivitySeverity.SUCCESS, "Queued " + actText(kind).lowercase() + " synced at " + locText(location) + ".", location)

    fun logSyncDropped(kind: ActivityKind) =
        log(ActivityActor.SYS, ActivityKind.SYNC, ActivitySeverity.FAILURE, "Queued " + actText(kind).lowercase() + " dropped (invalid).", null)

    // ── Background suite ─────────────────────────────────────────────────────────────────────────
    fun logTrigger(name: String) {
        if (!verbose) return // high-frequency, low-signal
        log(ActivityActor.SYS, ActivityKind.TRIGGER, ActivitySeverity.INFO, "Background evaluation ($name).", null)
    }

    fun logLocation(message: String, location: String? = null, severity: ActivitySeverity = ActivitySeverity.INFO) =
        log(ActivityActor.SYS, ActivityKind.LOCATION, severity, message, location)

    fun logAuth(message: String, severity: ActivitySeverity = ActivitySeverity.INFO) =
        log(ActivityActor.SYS, ActivityKind.AUTH, severity, message, null)

    fun logSystem(message: String, severity: ActivitySeverity = ActivitySeverity.INFO) =
        log(ActivityActor.SYS, ActivityKind.SYSTEM, severity, message, null)

    fun logWarning(message: String) =
        log(ActivityActor.SYS, ActivityKind.SYSTEM, ActivitySeverity.WARNING, message, null)

    fun logError(message: String) =
        log(ActivityActor.SYS, ActivityKind.ERROR, ActivitySeverity.FAILURE, message, null)

    // ── Core ─────────────────────────────────────────────────────────────────────────────────────
    private fun log(
        actor: ActivityActor,
        kind: ActivityKind,
        severity: ActivitySeverity,
        description: String,
        location: String?,
    ) {
        // Outer guard: building the entry / scheduling the write must never throw into the caller.
        runCatching {
            val entry = ActivityLogEntry(clock.now(), actor, kind, severity, description, location)
            appScope.launch {
                // Inner guard: a persistence failure (disk/serialization) is swallowed — best-effort.
                runCatching { activityLog.record(entry) }
            }
        }
    }

    private fun locText(location: String?): String = location?.takeIf { it.isNotBlank() } ?: "an unknown location"

    private fun detailSuffix(detail: String?): String = detail?.takeIf { it.isNotBlank() }?.let { " ($it)" } ?: ""

    private fun actText(kind: ActivityKind): String = when (kind) {
        ActivityKind.CHECK_IN -> "Check-in"
        ActivityKind.CHECK_OUT -> "Check-out"
        else -> "Activity"
    }
}
