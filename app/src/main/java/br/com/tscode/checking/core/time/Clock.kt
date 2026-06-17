package br.com.tscode.checking.core.time

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

// Injectable clock — lets tests provide a fixed instant (§10.2).
interface Clock {
    fun now(): Instant
    fun nowInZone(zone: ZoneId = SINGAPORE): ZonedDateTime = now().atZone(zone)

    companion object {
        val SINGAPORE: ZoneId = ZoneId.of("Asia/Singapore")
    }
}

// Production implementation — delegates to the system clock.
class SystemClock : Clock {
    override fun now(): Instant = Instant.now()
}
