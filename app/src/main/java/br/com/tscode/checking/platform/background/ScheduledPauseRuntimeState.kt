package br.com.tscode.checking.platform.background

import br.com.tscode.checking.domain.checkrules.ScheduledPauseSettings
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Identity of one concrete scheduled-pause occurrence.
 *
 * The configured fields make an edit invalidate the old occurrence, while [resumeAtEpochMs]
 * distinguishes consecutive windows with the same configuration. User and project identity keep a
 * delayed/active pause from leaking across authentication or membership changes.
 */
@Serializable
internal data class ScheduledPauseOccurrence(
    val chave: String,
    val project: String,
    val scheduledPauseEnabled: Boolean,
    val scheduledPauseFrom: String,
    val scheduledPauseTo: String,
    val suspendSaturdays: Boolean,
    val suspendSundays: Boolean,
    val zoneId: String,
    val startAtEpochMs: Long,
    val resumeAtEpochMs: Long,
)

@Serializable
internal enum class ScheduledPauseRuntimePhase {
    AWAITING_CHECKOUT,
    GRACE,
    ACTIVE,
    // Checkout was confirmed too close to the occurrence end for ten full seconds of grace.
    // Keep the occurrence identity until resume so a later trigger cannot start it prematurely.
    SKIPPED,
}

@Serializable
internal data class ScheduledPauseRuntimeState(
    val occurrence: ScheduledPauseOccurrence,
    val phase: ScheduledPauseRuntimePhase,
    // Meaningful only for GRACE. The original deadline is never extended by duplicate callbacks.
    val activateAtEpochMs: Long? = null,
)

internal fun scheduledPauseOccurrence(
    chave: String,
    project: String,
    settings: ScheduledPauseSettings,
    zoneId: String,
    startAt: Instant?,
    resumeAt: Instant?,
): ScheduledPauseOccurrence? {
    if (chave.isEmpty() || project.isEmpty() || resumeAt == null) return null
    return ScheduledPauseOccurrence(
        chave = chave,
        project = project,
        scheduledPauseEnabled = settings.scheduledPauseEnabled,
        scheduledPauseFrom = settings.scheduledPauseFrom,
        scheduledPauseTo = settings.scheduledPauseTo,
        suspendSaturdays = settings.suspendSaturdays,
        suspendSundays = settings.suspendSundays,
        zoneId = zoneId,
        // A missing start candidate must not disable the pause gate (notably around unusual
        // timezone rules). MIN_VALUE conservatively treats a later checkout as in-occurrence.
        startAtEpochMs = startAt?.toEpochMilli() ?: Long.MIN_VALUE,
        resumeAtEpochMs = resumeAt.toEpochMilli(),
    )
}
