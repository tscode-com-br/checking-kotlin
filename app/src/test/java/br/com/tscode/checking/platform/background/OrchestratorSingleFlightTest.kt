package br.com.tscode.checking.platform.background

import android.content.Context
import android.os.PowerManager
import br.com.tscode.checking.core.time.Clock
import br.com.tscode.checking.data.local.AppPreferencesDataSource
import br.com.tscode.checking.data.local.SecurePasswordStore
import br.com.tscode.checking.domain.repository.AccidentRepository
import br.com.tscode.checking.domain.repository.AuthRepository
import br.com.tscode.checking.domain.repository.CheckRepository
import br.com.tscode.checking.domain.usecase.RunAutomaticActivitiesUseCase
import br.com.tscode.checking.platform.location.LocationProvider
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/**
 * TP3 (test 2) — single-flight guard: while one `runOnce` holds the orchestrator `Mutex`, a concurrent
 * `runOnce` returns immediately (`tryLock` fails) and does NOT submit. Run 1 is held at its first suspend
 * point (`appPrefs.chave.first()`) via a gate, so it keeps the mutex; run 2 must complete without waiting.
 */
class OrchestratorSingleFlightTest {

    private val wakeLock = mockk<PowerManager.WakeLock>(relaxed = true)
    private val powerManager = mockk<PowerManager> { every { newWakeLock(any(), any()) } returns wakeLock }
    private val context = mockk<Context> {
        every { getSystemService(Context.POWER_SERVICE) } returns powerManager
    }
    private val appPrefs = mockk<AppPreferencesDataSource>(relaxed = true)
    private val checkRepository = mockk<CheckRepository>(relaxed = true)
    private val useCase = mockk<RunAutomaticActivitiesUseCase>(relaxed = true)
    private val locationProvider = mockk<LocationProvider>(relaxed = true)
    private val clock = mockk<Clock> { every { now() } returns Instant.EPOCH }
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val securePasswordStore = mockk<SecurePasswordStore>(relaxed = true)
    private val accidentRepository = mockk<AccidentRepository>(relaxed = true)

    private val orchestrator = BackgroundCheckOrchestrator(
        context, appPrefs, checkRepository, useCase, locationProvider, clock,
        authRepository, securePasswordStore, accidentRepository,
        mockk(relaxed = true),
    )

    @Test
    fun `concurrent_runOnce_is_blocked_by_single_flight`() = runTest {
        // Gate run 1 at its first suspend point so it holds the mutex until we release it. Empty chave
        // then makes runOnceLocked return early (no other deps needed).
        val gate = CompletableDeferred<String>()
        every { appPrefs.chave } returns flow { emit(gate.await()) }

        val run1 = launch { orchestrator.runOnce(OrchestratorTrigger.TIMER) }
        advanceUntilIdle()
        assertFalse("run1 should be suspended holding the mutex", run1.isCompleted)

        val run2 = launch { orchestrator.runOnce(OrchestratorTrigger.GEOFENCE) }
        advanceUntilIdle()
        // Single-flight: run2's tryLock fails → it returns immediately even though run1 still holds the
        // mutex. If the guard were broken, run2 would also suspend on the gate and NOT complete here.
        assertTrue("run2 must return immediately (single-flight)", run2.isCompleted)
        assertFalse("run1 must still hold the mutex", run1.isCompleted)

        gate.complete("") // release run1 → empty chave → runOnceLocked returns → mutex unlocked
        advanceUntilIdle()
        assertTrue(run1.isCompleted)

        coVerify(exactly = 0) { checkRepository.submit(any(), any(), any(), any(), any(), any(), any()) }
    }
}
