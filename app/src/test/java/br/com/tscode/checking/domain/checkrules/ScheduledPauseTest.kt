package br.com.tscode.checking.domain.checkrules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

// Fixed UTC zone — avoids DST edge-cases while testing pure time arithmetic.
// 2024-01-13 = Saturday, 2024-01-14 = Sunday, 2024-01-15 = Monday.
class ScheduledPauseTest {

    private val zone = ZoneId.of("UTC")

    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int): ZonedDateTime =
        ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zone)

    // Helpers for common dates
    private fun satAt(h: Int, m: Int) = at(2024, 1, 13, h, m)   // Saturday
    private fun sunAt(h: Int, m: Int) = at(2024, 1, 14, h, m)   // Sunday
    private fun monAt(h: Int, m: Int) = at(2024, 1, 15, h, m)   // Monday
    private fun tueAt(h: Int, m: Int) = at(2024, 1, 16, h, m)   // Tuesday

    // --- Fully disabled ---

    @Test fun `all disabled - never paused`() {
        val s = settings()
        assertFalse(isScheduledPauseActiveNow(monAt(10, 0), s))
        assertFalse(isScheduledPauseActiveNow(satAt(10, 0), s))
        assertFalse(isScheduledPauseActiveNow(sunAt(10, 0), s))
    }

    // --- Same-day window (f < t): 09:00–17:00 ---

    @Test fun `same-day window - just before start - not paused`() {
        val s = settings(enabled = true, from = "09:00", to = "17:00")
        assertFalse(isScheduledPauseActiveNow(monAt(8, 59), s))
    }

    @Test fun `same-day window - at start boundary - paused`() {
        val s = settings(enabled = true, from = "09:00", to = "17:00")
        assertTrue(isScheduledPauseActiveNow(monAt(9, 0), s))
    }

    @Test fun `same-day window - mid window - paused`() {
        val s = settings(enabled = true, from = "09:00", to = "17:00")
        assertTrue(isScheduledPauseActiveNow(monAt(12, 30), s))
    }

    @Test fun `same-day window - one minute before end - paused`() {
        val s = settings(enabled = true, from = "09:00", to = "17:00")
        assertTrue(isScheduledPauseActiveNow(monAt(16, 59), s))
    }

    @Test fun `same-day window - at end boundary - not paused`() {
        val s = settings(enabled = true, from = "09:00", to = "17:00")
        assertFalse(isScheduledPauseActiveNow(monAt(17, 0), s))
    }

    @Test fun `same-day window - after end - not paused`() {
        val s = settings(enabled = true, from = "09:00", to = "17:00")
        assertFalse(isScheduledPauseActiveNow(monAt(20, 0), s))
    }

    // --- Wrap-midnight window (f > t): 22:00–06:00 ---

    @Test fun `wrap-midnight - just before evening start - not paused`() {
        val s = settings(enabled = true, from = "22:00", to = "06:00")
        assertFalse(isScheduledPauseActiveNow(monAt(21, 59), s))
    }

    @Test fun `wrap-midnight - at evening start - paused`() {
        val s = settings(enabled = true, from = "22:00", to = "06:00")
        assertTrue(isScheduledPauseActiveNow(monAt(22, 0), s))
    }

    @Test fun `wrap-midnight - last minute of day - paused`() {
        val s = settings(enabled = true, from = "22:00", to = "06:00")
        assertTrue(isScheduledPauseActiveNow(monAt(23, 59), s))
    }

    @Test fun `wrap-midnight - midnight - paused`() {
        val s = settings(enabled = true, from = "22:00", to = "06:00")
        assertTrue(isScheduledPauseActiveNow(tueAt(0, 0), s))
    }

    @Test fun `wrap-midnight - morning before end - paused`() {
        val s = settings(enabled = true, from = "22:00", to = "06:00")
        assertTrue(isScheduledPauseActiveNow(tueAt(5, 59), s))
    }

    @Test fun `wrap-midnight - at morning end boundary - not paused`() {
        val s = settings(enabled = true, from = "22:00", to = "06:00")
        assertFalse(isScheduledPauseActiveNow(tueAt(6, 0), s))
    }

    // --- Equal endpoints: 09:00–09:00 (ambiguous — treated as no time-based pause) ---

    @Test fun `equal endpoints - never paused by window`() {
        val s = settings(enabled = true, from = "09:00", to = "09:00")
        assertFalse(isScheduledPauseActiveNow(monAt(9, 0), s))
        assertFalse(isScheduledPauseActiveNow(monAt(0, 0), s))
        assertFalse(isScheduledPauseActiveNow(monAt(23, 59), s))
    }

    // --- Weekend suspension ---

    @Test fun `suspend saturdays - Saturday is paused`() {
        val s = settings(suspendSat = true)
        assertTrue(isScheduledPauseActiveNow(satAt(0, 0), s))
        assertTrue(isScheduledPauseActiveNow(satAt(12, 0), s))
        assertTrue(isScheduledPauseActiveNow(satAt(23, 59), s))
    }

    @Test fun `suspend saturdays - Sunday not paused`() {
        val s = settings(suspendSat = true)
        assertFalse(isScheduledPauseActiveNow(sunAt(10, 0), s))
    }

    @Test fun `suspend sundays - Sunday is paused`() {
        val s = settings(suspendSun = true)
        assertTrue(isScheduledPauseActiveNow(sunAt(0, 0), s))
        assertTrue(isScheduledPauseActiveNow(sunAt(12, 0), s))
    }

    @Test fun `suspend sundays - Saturday not paused`() {
        val s = settings(suspendSun = true)
        assertFalse(isScheduledPauseActiveNow(satAt(10, 0), s))
    }

    @Test fun `suspend both weekends - both days paused`() {
        val s = settings(suspendSat = true, suspendSun = true)
        assertTrue(isScheduledPauseActiveNow(satAt(10, 0), s))
        assertTrue(isScheduledPauseActiveNow(sunAt(10, 0), s))
        assertFalse(isScheduledPauseActiveNow(monAt(10, 0), s))
    }

    // Weekend suspension is independent of scheduledPauseEnabled
    @Test fun `suspend saturday - window disabled - still paused on Saturday`() {
        val s = settings(suspendSat = true, enabled = false)
        assertTrue(isScheduledPauseActiveNow(satAt(12, 0), s))
    }

    // --- nextResumeInstant: null when not paused ---

    @Test fun `nextResumeInstant - not paused - returns null`() {
        val s = settings()
        assertNull(nextResumeInstant(monAt(10, 0), s))
    }

    @Test fun `nextResumeInstant - window disabled and no weekend - returns null`() {
        val s = settings(enabled = false)
        assertNull(nextResumeInstant(monAt(10, 0), s))
    }

    // --- nextResumeInstant: same-day window ---

    @Test fun `nextResumeInstant - same-day window paused - resumes today at end`() {
        val s = settings(enabled = true, from = "09:00", to = "17:00")
        val now = monAt(10, 0)
        val expected = monAt(17, 0).toInstant()
        assertEquals(expected, nextResumeInstant(now, s))
    }

    @Test fun `nextResumeInstant - same-day window at start boundary - resumes today at end`() {
        val s = settings(enabled = true, from = "09:00", to = "17:00")
        val now = monAt(9, 0)
        val expected = monAt(17, 0).toInstant()
        assertEquals(expected, nextResumeInstant(now, s))
    }

    // --- nextResumeInstant: wrap-midnight window ---

    @Test fun `nextResumeInstant - wrap-midnight in evening part - resumes tomorrow at end`() {
        // paused at Mon 23:00 with 22:00-06:00 → resume Tue 06:00
        val s = settings(enabled = true, from = "22:00", to = "06:00")
        val now = monAt(23, 0)
        val expected = tueAt(6, 0).toInstant()
        assertEquals(expected, nextResumeInstant(now, s))
    }

    @Test fun `nextResumeInstant - wrap-midnight in morning part - resumes today at end`() {
        // paused at Tue 02:00 with 22:00-06:00 → resume Tue 06:00
        val s = settings(enabled = true, from = "22:00", to = "06:00")
        val now = tueAt(2, 0)
        val expected = tueAt(6, 0).toInstant()
        assertEquals(expected, nextResumeInstant(now, s))
    }

    @Test fun `nextResumeInstant - wrap-midnight at midnight - resumes today at end`() {
        // paused at Tue 00:00 with 22:00-06:00 → resume Tue 06:00
        val s = settings(enabled = true, from = "22:00", to = "06:00")
        val now = tueAt(0, 0)
        val expected = tueAt(6, 0).toInstant()
        assertEquals(expected, nextResumeInstant(now, s))
    }

    // --- nextResumeInstant: weekend suspension only ---

    @Test fun `nextResumeInstant - Saturday suspended, Sunday not - resumes at midnight Sunday`() {
        val s = settings(suspendSat = true)
        val now = satAt(10, 0)
        val expected = sunAt(0, 0).toInstant()
        assertEquals(expected, nextResumeInstant(now, s))
    }

    @Test fun `nextResumeInstant - both days suspended - resumes at midnight Monday`() {
        val s = settings(suspendSat = true, suspendSun = true)
        val now = satAt(10, 0)
        val expected = monAt(0, 0).toInstant()
        assertEquals(expected, nextResumeInstant(now, s))
    }

    @Test fun `nextResumeInstant - Sunday suspended - resumes at midnight Monday`() {
        val s = settings(suspendSun = true)
        val now = sunAt(12, 0)
        val expected = monAt(0, 0).toInstant()
        assertEquals(expected, nextResumeInstant(now, s))
    }

    // --- nextResumeInstant: combined Saturday suspension + wrap-midnight window ---
    // Edge case: at 02:00 Saturday with suspendSat + window 22:00-06:00 (no Sunday suspension)
    // Resume candidates: Sat 06:00 (paused — Saturday), Sun 00:00 (not Saturday — but is window
    // active? n=0 < t=360 → PAUSED), Sun 06:00 (not Saturday, n=360 not < 360 → NOT PAUSED).

    @Test fun `nextResumeInstant - Saturday + wrap-midnight window, no Sunday suspension`() {
        val s = settings(enabled = true, from = "22:00", to = "06:00", suspendSat = true)
        val now = satAt(2, 0)
        val expected = sunAt(6, 0).toInstant()
        assertEquals(expected, nextResumeInstant(now, s))
    }

    // Edge case: Saturday + Sunday both suspended, window 22:00-06:00.
    // At Sat 02:00 → resume Mon 06:00 (Mon 00:00 still in wrap-midnight window).

    @Test fun `nextResumeInstant - both weekend days + wrap-midnight window`() {
        val s = settings(enabled = true, from = "22:00", to = "06:00", suspendSat = true, suspendSun = true)
        val now = satAt(2, 0)
        val expected = monAt(6, 0).toInstant()
        assertEquals(expected, nextResumeInstant(now, s))
    }

    // --- Helper ---

    private fun settings(
        enabled: Boolean = false,
        from: String = "22:00",
        to: String = "06:00",
        suspendSat: Boolean = false,
        suspendSun: Boolean = false,
    ) = ScheduledPauseSettings(
        scheduledPauseEnabled = enabled,
        scheduledPauseFrom = from,
        scheduledPauseTo = to,
        suspendSaturdays = suspendSat,
        suspendSundays = suspendSun,
    )
}
