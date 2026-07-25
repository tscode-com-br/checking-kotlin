package br.com.tscode.checking.platform.background

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccuracyRetryMovementGateTest {

    @Test
    fun `ordinary timer evaluates movement when no episode is active`() {
        assertTrue(shouldEvaluateTimerMovement(OrchestratorTrigger.TIMER, false))
    }

    @Test
    fun `ordinary timer bypasses movement skip while episode is active`() {
        assertFalse(shouldEvaluateTimerMovement(OrchestratorTrigger.TIMER, true))
    }

    @Test
    fun `accuracy retry never evaluates movement skip`() {
        assertFalse(shouldEvaluateTimerMovement(OrchestratorTrigger.ACCURACY_RETRY, false))
        assertFalse(shouldEvaluateTimerMovement(OrchestratorTrigger.ACCURACY_RETRY, true))
    }

    @Test
    fun `movement baseline accepts only finite readings within configured accuracy`() {
        assertTrue(isEligibleMovementBaseline(30.0, 30))
        assertFalse(isEligibleMovementBaseline(30.1, 30))
        assertFalse(isEligibleMovementBaseline(Double.NaN, 30))
        assertFalse(isEligibleMovementBaseline(Double.POSITIVE_INFINITY, 30))
    }
}
