package br.com.tscode.checking.presentation.check

import br.com.tscode.checking.domain.model.AuthStatus
import br.com.tscode.checking.domain.model.CheckAction
import br.com.tscode.checking.domain.model.UserProjects
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckUiStateProjectMembershipTest {

    private val authenticated = AuthStatus(
        found = true,
        chave = "1234",
        hasPassword = true,
        authenticated = true,
        message = "",
    )

    @Test
    fun `manual checkout is blocked when user has no project`() {
        val state = CheckUiState(
            authStatus = authenticated,
            selectedAction = CheckAction.CHECKOUT,
            automaticActivitiesEnabled = false,
            userProjects = UserProjects(emptyList(), ""),
        )

        assertFalse(state.canSubmit)
    }

    @Test
    fun `submit stays blocked until membership synchronization completes`() {
        val state = CheckUiState(
            authStatus = authenticated,
            selectedAction = CheckAction.CHECKOUT,
            automaticActivitiesEnabled = false,
            userProjects = UserProjects(listOf("P80"), "P80"),
            isProjectMembershipSyncing = true,
        )

        assertFalse(state.canSubmit)
        assertTrue(state.copy(isProjectMembershipSyncing = false).canSubmit)
    }
}
