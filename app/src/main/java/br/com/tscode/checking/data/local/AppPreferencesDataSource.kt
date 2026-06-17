package br.com.tscode.checking.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    companion object {
        val LANGUAGE = stringPreferencesKey("pref_language")
        val CHAVE = stringPreferencesKey("pref_chave")
        val USER_SETTINGS_JSON = stringPreferencesKey("pref_user_settings_json")
        val TRANSPORT_LOCAL_JSON = stringPreferencesKey("pref_transport_local_json")
        // Comma-separated active-accident ids last seen by the background watcher. Persisted
        // so accidents reported while the process was dead are still detected on the next run.
        val SEEN_ACCIDENT_IDS = stringPreferencesKey("pref_seen_accident_ids")
        // Offline check queue (P8): a JSON list of PendingCheckEvent awaiting sync. Persisted so
        // check-ins/outs captured while offline survive process death and are replayed on reconnect.
        val PENDING_CHECKS_JSON = stringPreferencesKey("pref_pending_checks_json")
    }

    val language: Flow<String> = dataStore.data.map { it[LANGUAGE] ?: "" }
    val chave: Flow<String> = dataStore.data.map { it[CHAVE] ?: "" }
    val userSettingsJson: Flow<String> = dataStore.data.map { it[USER_SETTINGS_JSON] ?: "" }
    val transportLocalJson: Flow<String> = dataStore.data.map { it[TRANSPORT_LOCAL_JSON] ?: "" }
    val pendingChecksJson: Flow<String> = dataStore.data.map { it[PENDING_CHECKS_JSON] ?: "" }
    val seenAccidentIds: Flow<Set<Int>> = dataStore.data.map { prefs ->
        (prefs[SEEN_ACCIDENT_IDS] ?: "")
            .split(',')
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()
    }

    suspend fun setLanguage(code: String) = dataStore.edit { it[LANGUAGE] = code }
    suspend fun setChave(chave: String) = dataStore.edit { it[CHAVE] = chave }
    suspend fun setUserSettingsJson(json: String) = dataStore.edit { it[USER_SETTINGS_JSON] = json }
    suspend fun setTransportLocalJson(json: String) = dataStore.edit { it[TRANSPORT_LOCAL_JSON] = json }
    suspend fun setPendingChecksJson(json: String) = dataStore.edit { it[PENDING_CHECKS_JSON] = json }
    suspend fun setSeenAccidentIds(ids: Set<Int>) =
        dataStore.edit { it[SEEN_ACCIDENT_IDS] = ids.joinToString(",") }

    fun getFlag(name: String): Flow<Boolean> =
        dataStore.data.map { it[booleanPreferencesKey("pref_flag_$name")] ?: false }

    suspend fun setFlag(name: String, value: Boolean) =
        dataStore.edit { it[booleanPreferencesKey("pref_flag_$name")] = value }
}
