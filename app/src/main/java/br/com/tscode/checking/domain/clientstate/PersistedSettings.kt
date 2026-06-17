package br.com.tscode.checking.domain.clientstate

import kotlinx.serialization.Serializable

@Serializable
data class UserSettings(
    val projects: List<String>,
    val activeProject: String,
    val automaticActivitiesEnabled: Boolean,
    val scheduledPauseEnabled: Boolean = false,
    val scheduledPauseFrom: String = "22:00",
    val scheduledPauseTo: String = "06:00",
    val suspendSaturdays: Boolean = false,
    val suspendSundays: Boolean = false,
    // Push-notification preferences (default on).
    val notifyActivities: Boolean = true,
    val notifyScheduledPause: Boolean = true,
    val notifyAccident: Boolean = true,
)

data class UserSettingsDefaults(
    val allowedProjects: List<String> = emptyList(),
    val projects: List<String> = emptyList(),
    val project: String = "",
    val activeProject: String = "",
    val automaticActivitiesEnabled: Boolean = false,
)

fun resolvePersistedPassword(passwordsByChave: Map<String, String>?, chave: String): String {
    val normalizedChave = sanitizeSettingsChave(chave)
    if (normalizedChave.length != 4) return ""
    val stored = passwordsByChave?.get(normalizedChave) ?: ""
    return if (isPasswordLengthValid(stored)) stored else ""
}

fun withPersistedPassword(
    passwordsByChave: Map<String, String>?,
    chave: String,
    password: String?,
): Map<String, String> {
    val normalizedChave = sanitizeSettingsChave(chave)
    val currentMap = passwordsByChave?.toMutableMap() ?: mutableMapOf()
    if (normalizedChave.length != 4) return currentMap
    return if (isPasswordLengthValid(password)) {
        currentMap.apply { put(normalizedChave, password!!) }
    } else {
        currentMap.apply { remove(normalizedChave) }
    }
}

fun resolvePersistedUserSettings(
    settingsByChave: Map<String, UserSettings?>?,
    chave: String,
    defaults: UserSettingsDefaults = UserSettingsDefaults(),
): UserSettings {
    val normalizedChave = sanitizeSettingsChave(chave)
    val fallbackProjects = resolveFallbackProjects(defaults)
    val fallbackActiveProject = resolveFallbackActiveProject(defaults, fallbackProjects)
    val fallbackAutoEnabled = defaults.automaticActivitiesEnabled
    if (normalizedChave.length != 4) {
        return UserSettings(
            projects = fallbackProjects,
            activeProject = fallbackActiveProject,
            automaticActivitiesEnabled = fallbackAutoEnabled,
        )
    }

    val record = settingsByChave?.get(normalizedChave)
    val resolvedProjects = normalizeProjectValues(
        values = if (record != null && record.projects.isNotEmpty()) record.projects
        else if (record != null && record.activeProject.isNotEmpty()) listOf(record.activeProject)
        else emptyList(),
        allowedProjects = defaults.allowedProjects,
        fallbackProjects = fallbackProjects,
    )
    val resolvedActiveProject = normalizeProjectValue(
        projectValue = record?.activeProject ?: "",
        allowedProjects = resolvedProjects,
        fallbackProject = resolveFallbackActiveProject(defaults, resolvedProjects),
    )

    return UserSettings(
        projects = resolvedProjects,
        activeProject = resolvedActiveProject,
        automaticActivitiesEnabled = record?.automaticActivitiesEnabled ?: fallbackAutoEnabled,
        scheduledPauseEnabled = record?.scheduledPauseEnabled ?: false,
        scheduledPauseFrom = record?.scheduledPauseFrom ?: "22:00",
        scheduledPauseTo = record?.scheduledPauseTo ?: "06:00",
        suspendSaturdays = record?.suspendSaturdays ?: false,
        suspendSundays = record?.suspendSundays ?: false,
        notifyActivities = record?.notifyActivities ?: true,
        notifyScheduledPause = record?.notifyScheduledPause ?: true,
        notifyAccident = record?.notifyAccident ?: true,
    )
}

fun withPersistedUserSettings(
    settingsByChave: Map<String, UserSettings?>?,
    chave: String,
    nextSettings: UserSettings,
    defaults: UserSettingsDefaults = UserSettingsDefaults(),
): Map<String, UserSettings?> {
    val normalizedChave = sanitizeSettingsChave(chave)
    val currentMap = settingsByChave?.toMutableMap() ?: mutableMapOf()
    if (normalizedChave.length != 4) return currentMap

    val fallbackProjects = resolveFallbackProjects(defaults)
    val resolvedProjects = normalizeProjectValues(
        values = nextSettings.projects.ifEmpty {
            if (nextSettings.activeProject.isNotEmpty()) listOf(nextSettings.activeProject) else emptyList()
        },
        allowedProjects = defaults.allowedProjects,
        fallbackProjects = fallbackProjects,
    )
    val resolvedActiveProject = normalizeProjectValue(
        projectValue = nextSettings.activeProject,
        allowedProjects = resolvedProjects,
        fallbackProject = resolveFallbackActiveProject(defaults, resolvedProjects),
    )

    currentMap[normalizedChave] = nextSettings.copy(
        projects = resolvedProjects,
        activeProject = resolvedActiveProject,
    )
    return currentMap
}
