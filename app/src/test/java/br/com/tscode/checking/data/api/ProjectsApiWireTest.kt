package br.com.tscode.checking.data.api

import br.com.tscode.checking.data.dto.WebUserProjectsUpdateRequest
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class ProjectsApiWireTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ProjectsApi
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/api/web/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ProjectsApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `PUT user-projects sends and accepts an empty membership list`() = runTest {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                      "projects": [],
                      "active_project": "",
                      "ok": true,
                      "message": "Projetos atualizados."
                    }
                    """.trimIndent(),
                ),
        )

        val response = api.updateUserProjects(WebUserProjectsUpdateRequest(projects = emptyList()))

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/web/user-projects", request.path)
        assertEquals(
            0,
            json.parseToJsonElement(request.body.readUtf8())
                .jsonObject.getValue("projects").jsonArray.size,
        )
        assertEquals(emptyList<String>(), response.projects)
        assertEquals("", response.activeProject)
    }

    @Test
    fun `GET user-projects decodes a user without projects`() = runTest {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""{"projects":[],"active_project":""}"""),
        )

        val response = api.getUserProjects()

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/web/user-projects", request.path)
        assertEquals(emptyList<String>(), response.projects)
        assertEquals("", response.activeProject)
    }
}
