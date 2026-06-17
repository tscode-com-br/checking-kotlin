package br.com.tscode.checking.domain.model

data class Project(
    val id: Int,
    val name: String,
    val transportEnabled: Boolean,
)

data class UserProjects(
    val projects: List<String>,
    val activeProject: String,
)
