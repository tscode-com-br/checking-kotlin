package br.com.tscode.checking.data.repository

import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.data.api.AuthApi
import br.com.tscode.checking.data.api.CheckApi
import br.com.tscode.checking.data.dto.WebPasswordStatusResponse
import br.com.tscode.checking.data.dto.WebUserSelfRegistrationResponse
import br.com.tscode.checking.data.local.PersistentCookieJar
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * TP4 (#1) — AuthRepositoryImpl maps the plan003 contract fields onto AuthStatus:
 *   getStatus → pending_approval; selfRegister status → pending / queue_full / registered.
 */
class AuthMappingTest {

    private val authApi = mockk<AuthApi>()
    private val checkApi = mockk<CheckApi>(relaxed = true)
    private val cookieJar = mockk<PersistentCookieJar>(relaxed = true)
    private val repo = AuthRepositoryImpl(authApi, checkApi, cookieJar)

    private fun statusDto(found: Boolean, hasPassword: Boolean, pendingApproval: Boolean) =
        WebPasswordStatusResponse(
            found = found, chave = "NEW1", hasPassword = hasPassword, authenticated = false,
            message = "m", pendingApproval = pendingApproval,
        )

    private fun selfRegDto(status: String, authenticated: Boolean, pending: Boolean, queueFull: Boolean) =
        WebUserSelfRegistrationResponse(
            ok = true, authenticated = authenticated, hasPassword = authenticated, message = "m",
            status = status, pendingApproval = pending, queueFull = queueFull,
            projects = listOf("P80"), activeProject = if (authenticated) "P80" else "",
        )

    @Test
    fun getStatus_maps_pending_approval_true() = runTest {
        coEvery { authApi.getStatus("NEW1") } returns statusDto(found = false, hasPassword = false, pendingApproval = true)
        val s = (repo.getStatus("NEW1") as AppResult.Success).data
        assertFalse(s.found)
        assertFalse(s.authenticated)
        assertTrue(s.pendingApproval)
    }

    @Test
    fun getStatus_maps_pending_approval_false_for_normal_user() = runTest {
        coEvery { authApi.getStatus("HR70") } returns statusDto(found = true, hasPassword = true, pendingApproval = false)
        val s = (repo.getStatus("HR70") as AppResult.Success).data
        assertTrue(s.found)
        assertFalse(s.pendingApproval)
    }

    @Test
    fun selfRegister_pending_maps_to_not_found_pending() = runTest {
        coEvery { authApi.registerUser(any()) } returns selfRegDto("pending", authenticated = false, pending = true, queueFull = false)
        val s = (repo.selfRegister("NEW1", "Nome Completo", listOf("P80"), null, "abc123", "abc123") as AppResult.Success).data
        assertFalse(s.found)
        assertFalse(s.authenticated)
        assertTrue(s.pendingApproval)
        assertFalse(s.queueFull)
    }

    @Test
    fun selfRegister_queue_full_maps_to_queueFull() = runTest {
        coEvery { authApi.registerUser(any()) } returns selfRegDto("queue_full", authenticated = false, pending = false, queueFull = true)
        val s = (repo.selfRegister("NEW1", "Nome Completo", listOf("P80"), null, "abc123", "abc123") as AppResult.Success).data
        assertFalse(s.found)
        assertFalse(s.authenticated)
        assertTrue(s.queueFull)
        assertFalse(s.pendingApproval)
    }

    @Test
    fun selfRegister_registered_maps_to_found_authenticated() = runTest {
        coEvery { authApi.registerUser(any()) } returns selfRegDto("registered", authenticated = true, pending = false, queueFull = false)
        val s = (repo.selfRegister("NEW1", "Nome Completo", listOf("P80"), null, "abc123", "abc123") as AppResult.Success).data
        assertTrue(s.found)
        assertTrue(s.authenticated)
        assertFalse(s.pendingApproval)
        assertFalse(s.queueFull)
    }
}
