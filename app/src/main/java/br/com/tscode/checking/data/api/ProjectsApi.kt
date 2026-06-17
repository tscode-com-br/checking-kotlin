package br.com.tscode.checking.data.api

import br.com.tscode.checking.data.dto.ProjectRow
import br.com.tscode.checking.data.dto.WebProjectUpdateRequest
import br.com.tscode.checking.data.dto.WebProjectUpdateResponse
import br.com.tscode.checking.data.dto.WebUserProjectsResponse
import br.com.tscode.checking.data.dto.WebUserProjectsUpdateRequest
import br.com.tscode.checking.data.dto.WebUserProjectsUpdateResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface ProjectsApi {
    @GET("projects")
    suspend fun listProjects(): List<ProjectRow>

    @GET("user-projects")
    suspend fun getUserProjects(): WebUserProjectsResponse

    @PUT("user-projects")
    suspend fun updateUserProjects(@Body body: WebUserProjectsUpdateRequest): WebUserProjectsUpdateResponse

    @PUT("project")
    suspend fun updateActiveProject(@Body body: WebProjectUpdateRequest): WebProjectUpdateResponse
}
