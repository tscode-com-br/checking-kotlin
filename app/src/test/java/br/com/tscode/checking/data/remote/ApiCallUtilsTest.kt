package br.com.tscode.checking.data.remote

import br.com.tscode.checking.core.error.ApiError
import br.com.tscode.checking.core.result.AppResult
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ApiCallUtilsTest {
    @Test
    fun `extracts string detail from FastAPI body`() {
        val detail =
            extractApiErrorDetail(
                """{"detail":"O projeto informado não pertence ao usuário."}""",
            )

        assertEquals("O projeto informado não pertence ao usuário.", detail)
    }

    @Test
    fun `keeps structured FastAPI detail as JSON`() {
        val detail =
            extractApiErrorDetail(
                """{"detail":[{"loc":["body","projects"],"msg":"Field required","type":"missing"}]}""",
            )

        assertEquals(
            """[{"loc":["body","projects"],"msg":"Field required","type":"missing"}]""",
            detail,
        )
    }

    @Test
    fun `falls back to non-JSON response body and handles an empty body`() {
        assertEquals("Service unavailable", extractApiErrorDetail("  Service unavailable  "))
        assertNull(extractApiErrorDetail("  "))
        assertNull(extractApiErrorDetail(null))
    }

    @Test
    fun `safeApiCall maps 409 to conflict and preserves detail`() =
        runTest {
            val result =
                safeApiCall<Unit> {
                    throw httpException(
                        409,
                        """{"detail":"O projeto informado nao pertence aos projetos cadastrados do usuario."}""",
                    )
                }

            assertTrue(result is AppResult.Failure)
            val failure = result as AppResult.Failure
            assertTrue(failure.error is ApiError.Conflict)
            val conflict = failure.error as ApiError.Conflict
            assertEquals(
                "O projeto informado nao pertence aos projetos cadastrados do usuario.",
                conflict.detail,
            )
        }

    @Test
    fun `safeApiCall maps other HTTP status and preserves extracted detail`() =
        runTest {
            val result =
                safeApiCall<Unit> {
                    throw httpException(422, """{"detail":"Selecione ao menos um projeto."}""")
                }

            assertTrue(result is AppResult.Failure)
            val failure = result as AppResult.Failure
            assertTrue(failure.error is ApiError.Http)
            val error = failure.error as ApiError.Http
            assertEquals(422, error.status)
            assertEquals("Selecione ao menos um projeto.", error.detail)
        }

    private fun httpException(
        status: Int,
        body: String,
    ): HttpException =
        HttpException(
            Response.error<Unit>(
                status,
                body.toResponseBody("application/json".toMediaType()),
            ),
        )
}
