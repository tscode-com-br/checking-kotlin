package br.com.tscode.checking.data.repository

import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.data.api.ProjectsApi
import br.com.tscode.checking.data.dto.WebProjectUpdateRequest
import br.com.tscode.checking.data.dto.WebUserProjectsUpdateRequest
import br.com.tscode.checking.data.remote.safeApiCall
import br.com.tscode.checking.domain.model.Project
import br.com.tscode.checking.domain.model.UserProjects
import br.com.tscode.checking.domain.repository.ProjectRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepositoryImpl @Inject constructor(
    private val projectsApi: ProjectsApi,
) : ProjectRepository {

    override suspend fun listProjects(): AppResult<List<Project>> = safeApiCall {
        projectsApi.listProjects().map { row ->
            Project(id = row.id, name = row.name, transportEnabled = row.transportEnabled)
        }
    }

    override suspend fun getUserProjects(): AppResult<UserProjects> = safeApiCall {
        val r = projectsApi.getUserProjects()
        UserProjects(projects = r.projects, activeProject = r.activeProject)
    }

    override suspend fun updateUserProjects(projectNames: List<String>): AppResult<UserProjects> = safeApiCall {
        val r = projectsApi.updateUserProjects(WebUserProjectsUpdateRequest(projects = projectNames))
        UserProjects(projects = r.projects, activeProject = r.activeProject)
    }

    override suspend fun updateActiveProject(projectName: String): AppResult<UserProjects> = safeApiCall {
        val r = projectsApi.updateActiveProject(WebProjectUpdateRequest(project = projectName))
        UserProjects(projects = r.projects, activeProject = r.activeProject)
    }
}
