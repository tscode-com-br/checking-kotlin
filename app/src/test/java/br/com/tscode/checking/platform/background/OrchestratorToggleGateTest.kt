package br.com.tscode.checking.platform.background

import android.content.Context
import android.os.PowerManager
import br.com.tscode.checking.core.error.ApiError
import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.core.time.Clock
import br.com.tscode.checking.data.local.AppPreferencesDataSource
import br.com.tscode.checking.data.local.SecurePasswordStore
import br.com.tscode.checking.domain.repository.AccidentRepository
import br.com.tscode.checking.domain.repository.AuthRepository
import br.com.tscode.checking.domain.repository.CheckRepository
import br.com.tscode.checking.domain.usecase.RunAutomaticActivitiesUseCase
import br.com.tscode.checking.platform.background.diagnostics.EvaluationLog
import br.com.tscode.checking.platform.background.diagnostics.EvaluationOutcome
import br.com.tscode.checking.platform.location.LocationProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.Instant

/**
 * TP4 (test 2) — Situação 9: the orchestrator's auto-activities toggle gate. With
 * `automaticActivitiesEnabled = false`, `runOnceLocked` records `EvaluationOutcome.TOGGLE_OFF` and returns
 * BEFORE any GPS capture / engine call / submit. Verified via the EvaluationLog diagnostics seam.
 */
class OrchestratorToggleGateTest {

    private val wakeLock = mockk<PowerManager.WakeLock>(relaxed = true)
    private val powerManager = mockk<PowerManager> { every { newWakeLock(any(), any()) } returns wakeLock }
    private val context = mockk<Context> {
        every { getSystemService(Context.POWER_SERVICE) } returns powerManager
    }
    private val appPrefs = mockk<AppPreferencesDataSource>(relaxed = true)
    private val checkRepository = mockk<CheckRepository>(relaxed = true)
    private val useCase = mockk<RunAutomaticActivitiesUseCase>(relaxed = true)
    private val locationProvider = mockk<LocationProvider>(relaxed = true)
    private val clock = mockk<Clock>()
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val securePasswordStore = mockk<SecurePasswordStore>(relaxed = true)
    private val accidentRepository = mockk<AccidentRepository>(relaxed = true)

    private val orchestrator = BackgroundCheckOrchestrator(
        context, appPrefs, checkRepository, useCase, locationProvider, clock,
        authRepository, securePasswordStore, accidentRepository,
        mockk(relaxed = true),
    )

    @Test
    fun `auto_off_records_toggle_off_and_never_submits`() = runTest {
        val at = Instant.parse("2026-06-18T09:09:09Z") // unique → identifies THIS run's log entry
        every { clock.now() } returns at
        every { appPrefs.chave } returns flowOf("HR70")
        every { appPrefs.language } returns flowOf("pt")
        every { appPrefs.userSettingsJson } returns flowOf("") // empty → default UserSettings (auto OFF)
        // notifyAccident defaults true → maybeNotifyAccident reads the accident state; keep it a no-op.
        coEvery { accidentRepository.getState(any()) } returns AppResult.Failure(ApiError.Network)

        orchestrator.runOnce(OrchestratorTrigger.FOREGROUND)

        val entry = EvaluationLog.snapshot().firstOrNull {
            it.at == at && it.trigger == OrchestratorTrigger.FOREGROUND
        }
        assertNotNull("expected a TOGGLE_OFF evaluation entry for this run", entry)
        assertEquals(EvaluationOutcome.TOGGLE_OFF, entry!!.outcome)
        // No GPS / engine / submit beyond the gate.
        coVerify(exactly = 0) { useCase(any(), any(), any(), any(), any()) }
        coVerify(exactly = 0) { checkRepository.submit(any(), any(), any(), any(), any(), any(), any()) }
    }
}
