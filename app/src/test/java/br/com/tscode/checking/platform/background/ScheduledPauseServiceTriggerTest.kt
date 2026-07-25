package br.com.tscode.checking.platform.background

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScheduledPauseServiceTriggerTest {

    @Test
    fun `alarm service actions map to guaranteed pause triggers`() {
        assertEquals(
            OrchestratorTrigger.PAUSE_START,
            scheduledPauseTriggerForServiceAction(AutoActivityForegroundService.ACTION_PAUSE_START),
        )
        assertEquals(
            OrchestratorTrigger.PAUSE_END,
            scheduledPauseTriggerForServiceAction(AutoActivityForegroundService.ACTION_PAUSE_END),
        )
        assertEquals(
            OrchestratorTrigger.PAUSE_GRACE,
            scheduledPauseTriggerForServiceAction(AutoActivityForegroundService.ACTION_PAUSE_GRACE),
        )
        assertNull(scheduledPauseTriggerForServiceAction(null))
    }
}
