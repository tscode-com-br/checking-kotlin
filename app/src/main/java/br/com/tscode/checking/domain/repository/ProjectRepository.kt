package br.com.tscode.checking.domain.repository

import br.com.tscode.checking.core.result.AppResult
import br.com.tscode.checking.domain.model.Project
import br.com.tscode.checking.domain.model.UserProjects

interface ProjectRepository {
    suspend fun listProjects(): AppResult<List<Project>>
    suspend fun getUserProjects(): AppResult<UserProjects>
    suspend fun updateUserProjects(projectNames: List<String>): AppResult<UserProjects>
    suspend fun updateActiveProject(projectName: String): AppResult<UserProjects>
}
