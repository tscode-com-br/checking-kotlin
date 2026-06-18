package br.com.tscode.checking.presentation.check

import br.com.tscode.checking.presentation.components.FieldGlow
import org.junit.Assert.assertEquals
import org.junit.Test

// P2.1 — pure mapping AutoActivitiesHealth.toGlow() → FieldGlow. No Android/permission APIs involved.
class AutoActivitiesHealthTest {

    @Test
    fun off_maps_to_no_glow() {
        assertEquals(FieldGlow.None, AutoActivitiesHealth.Off.toGlow())
    }

    @Test
    fun healthy_maps_to_authenticated_glow() {
        assertEquals(FieldGlow.Authenticated, AutoActivitiesHealth.Healthy.toGlow())
    }

    @Test
    fun degraded_maps_to_pending_glow() {
        assertEquals(FieldGlow.Pending, AutoActivitiesHealth.Degraded.toGlow())
    }
}
