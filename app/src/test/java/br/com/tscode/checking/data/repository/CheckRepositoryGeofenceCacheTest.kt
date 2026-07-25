package br.com.tscode.checking.data.repository

import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.core.time.Clock
import br.com.tscode.checking.data.api.CheckApi
import br.com.tscode.checking.data.dto.GeofenceCircleDto
import br.com.tscode.checking.data.dto.WebGeofencesResponse
import br.com.tscode.checking.data.remote.sse.CheckEventStream
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class CheckRepositoryGeofenceCacheTest {

    @Test
    fun `membership invalidation forces geofences to be fetched again for same chave`() = runTest {
        val api = mockk<CheckApi>()
        val clock = mockk<Clock> {
            every { now() } returns Instant.parse("2026-07-24T00:00:00Z")
        }
        val first = WebGeofencesResponse(listOf(circle(1, "Projeto antigo")))
        val second = WebGeofencesResponse(listOf(circle(2, "Projeto novo")))
        coEvery { api.getGeofences("1234") } returnsMany listOf(first, second)

        val repository = CheckRepositoryImpl(
            checkApi = api,
            clock = clock,
            checkEventStream = mockk<CheckEventStream>(relaxed = true),
        )

        val initial = repository.getGeofences("1234")
        val cached = repository.getGeofences("1234")
        repository.invalidateGeofenceCache()
        val refreshed = repository.getGeofences("1234")

        assertTrue(initial is AppResult.Success)
        assertEquals(initial, cached)
        assertTrue(refreshed is AppResult.Success)
        assertEquals(2, (refreshed as AppResult.Success).data.single().id)
        coVerify(exactly = 2) { api.getGeofences("1234") }
    }

    private fun circle(id: Int, local: String) = GeofenceCircleDto(
        id = id,
        local = local,
        centerLat = 0.0,
        centerLng = 0.0,
        radiusMeters = 100.0,
    )
}
