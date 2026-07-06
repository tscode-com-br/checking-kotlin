package br.com.tscode.checking.platform.background

import br.com.tscode.checking.core.error.ApiError
import br.com.tscode.checking.domain.model.LocationOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Offline capture resilience (P8). The engine's LocationOptions fetch must NOT bail the whole run when
 * the device is offline — it should return usable options so the run reaches the capture step and queues
 * an offline Raw for every movement / geofence transition. Regression guard for the "only the first
 * offline activity syncs, and the real check-out time is lost" bug: previously any failure (incl.
 * ApiError.Network) returned null and `getLocationOptions() ?: return` bailed, so once the 15-min options
 * cache expired no further offline reading was captured.
 */
class OfflineFallbackLocationOptionsTest {

    private val cached = LocationOptions(
        items = listOf("Unidade P80"),
        accuracyThresholdMeters = 45,
        mixedZoneIntervalMinutes = 20,
    )

    @Test
    fun `offline reuses the last cached options`() {
        // Network loss with a warm cache → keep the real threshold/interval, keep capturing.
        assertSame(cached, offlineFallbackLocationOptions(cached, ApiError.Network))
    }

    @Test
    fun `offline with no cache falls back to sane defaults`() {
        val result = offlineFallbackLocationOptions(null, ApiError.Network)
        assertEquals(DEFAULT_ACCURACY_THRESHOLD_METERS, result?.accuracyThresholdMeters)
        assertEquals(0, result?.mixedZoneIntervalMinutes)
    }

    @Test
    fun `session expiry bails (null) so the run can re-login`() {
        assertNull(offlineFallbackLocationOptions(cached, ApiError.Unauthorized))
    }

    @Test
    fun `server error bails (null) rather than capturing against a flaky server`() {
        assertNull(offlineFallbackLocationOptions(cached, ApiError.Http(500, "boom")))
        assertNull(offlineFallbackLocationOptions(cached, ApiError.Conflict))
        assertNull(offlineFallbackLocationOptions(cached, ApiError.Unknown(RuntimeException("x"))))
    }
}
