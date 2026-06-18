package br.com.tscode.checking.presentation.check

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// P5.1 — pure predicate shouldShowAutoActivitiesNudge(authenticated, autoEnabled, dismissed).
// No Android/persistence APIs involved; the per-chave flag read happens in the ViewModel.
class AutoActivitiesNudgeTest {

    @Test
    fun shows_when_authenticated_autoOff_andNotDismissed() {
        assertTrue(shouldShowAutoActivitiesNudge(authenticated = true, autoEnabled = false, dismissed = false))
    }

    @Test
    fun hidden_when_notAuthenticated() {
        assertFalse(shouldShowAutoActivitiesNudge(authenticated = false, autoEnabled = false, dismissed = false))
    }

    @Test
    fun hidden_when_autoActivitiesAlreadyEnabled() {
        assertFalse(shouldShowAutoActivitiesNudge(authenticated = true, autoEnabled = true, dismissed = false))
    }

    @Test
    fun hidden_when_alreadyDismissed() {
        assertFalse(shouldShowAutoActivitiesNudge(authenticated = true, autoEnabled = false, dismissed = true))
    }

    @Test
    fun hidden_when_everythingFalseButDismissed() {
        // Dismissed dominates even if the other two would otherwise show it.
        assertFalse(shouldShowAutoActivitiesNudge(authenticated = true, autoEnabled = true, dismissed = true))
    }
}
