package br.com.tscode.checking.domain.checkrules

import java.time.DayOfWeek
import java.time.Instant
import java.time.ZonedDateTime

// The 5 persisted fields that define the pause configuration (§23.4.2).
// Constructed from PersistedSettings at the call site.
data class ScheduledPauseSettings(
    val scheduledPauseEnabled: Boolean,
    val scheduledPauseFrom: String,  // "HH:mm" — device-local time
    val scheduledPauseTo: String,    // "HH:mm" — device-local time
    val suspendSaturdays: Boolean,
    val suspendSundays: Boolean,
)

// Returns true if automatic activities should be completely idle right now.
// Evaluation order matches §23.4.2:
//   1. Weekend suspension (full day, checked first — independent of time window).
//   2. Time window (only when scheduledPauseEnabled).
fun isScheduledPauseActiveNow(now: ZonedDateTime, settings: ScheduledPauseSettings): Boolean {
    if (settings.suspendSaturdays && now.dayOfWeek == DayOfWeek.SATURDAY) return true
    if (settings.suspendSundays && now.dayOfWeek == DayOfWeek.SUNDAY) return true

    if (settings.scheduledPauseEnabled) {
        val f = parseMinutesOfDay(settings.scheduledPauseFrom)
        val t = parseMinutesOfDay(settings.scheduledPauseTo)
        if (f != t) {
            val n = now.hour * 60 + now.minute
            return if (f < t) n in f until t else n >= f || n < t
        }
    }

    return false
}

// Returns the earliest Instant > now when isScheduledPauseActiveNow becomes false,
// or null if not currently paused. Used to schedule the exact-alarm resume (§23.4.2).
//
// Strategy: collect every candidate "end of a pause reason" moment in the next 8 days,
// sort ascending, and return the first that is not itself paused. This correctly handles
// overlap (e.g. both a time window and a weekend suspension active simultaneously).
fun nextResumeInstant(now: ZonedDateTime, settings: ScheduledPauseSettings): Instant? {
    if (!isScheduledPauseActiveNow(now, settings)) return null

    val candidates = mutableListOf<ZonedDateTime>()

    // All "window ends at t" occurrences for the next 8 days
    if (settings.scheduledPauseEnabled) {
        val f = parseMinutesOfDay(settings.scheduledPauseFrom)
        val t = parseMinutesOfDay(settings.scheduledPauseTo)
        if (f != t) {
            for (offset in 0L..7L) {
                val candidate = now.toLocalDate().plusDays(offset)
                    .atTime(t / 60, t % 60)
                    .atZone(now.zone)
                if (candidate.isAfter(now)) candidates.add(candidate)
            }
        }
    }

    // All "start of next day" boundaries for the next 7 days (weekend end transitions)
    for (offset in 1L..7L) {
        candidates.add(now.toLocalDate().plusDays(offset).atStartOfDay(now.zone))
    }

    return candidates
        .sortedWith(compareBy { it.toInstant() })
        .firstOrNull { !isScheduledPauseActiveNow(it, settings) }
        ?.toInstant()
}

// Returns the earliest Instant > now when isScheduledPauseActiveNow becomes TRUE (a transition
// INTO pause), or null if no pause is configured or we're already paused. Used to schedule the
// exact alarm that fires the pause-START notification precisely (mirrors nextResumeInstant).
fun nextPauseStartInstant(now: ZonedDateTime, settings: ScheduledPauseSettings): Instant? {
    if (isScheduledPauseActiveNow(now, settings)) return null

    val candidates = mutableListOf<ZonedDateTime>()

    // Time-window starts (the "from" time) for the next 8 days.
    if (settings.scheduledPauseEnabled) {
        val f = parseMinutesOfDay(settings.scheduledPauseFrom)
        val t = parseMinutesOfDay(settings.scheduledPauseTo)
        if (f != t) {
            for (offset in 0L..8L) {
                val candidate = now.toLocalDate().plusDays(offset)
                    .atTime(f / 60, f % 60)
                    .atZone(now.zone)
                if (candidate.isAfter(now)) candidates.add(candidate)
            }
        }
    }

    // Weekend-suspension starts (00:00 of a suspended Saturday/Sunday) for the next 8 days.
    for (offset in 0L..8L) {
        val day = now.toLocalDate().plusDays(offset)
        val startOfDay = day.atStartOfDay(now.zone)
        if (!startOfDay.isAfter(now)) continue
        val isSuspendedWeekendDay =
            (settings.suspendSaturdays && day.dayOfWeek == DayOfWeek.SATURDAY) ||
                (settings.suspendSundays && day.dayOfWeek == DayOfWeek.SUNDAY)
        if (isSuspendedWeekendDay) candidates.add(startOfDay)
    }

    return candidates
        .sortedWith(compareBy { it.toInstant() })
        .firstOrNull { isScheduledPauseActiveNow(it, settings) }
        ?.toInstant()
}

private fun parseMinutesOfDay(hhmm: String): Int {
    val colon = hhmm.indexOf(':')
    return hhmm.substring(0, colon).toInt() * 60 + hhmm.substring(colon + 1).toInt()
}
