package br.com.tscode.checking.platform.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import br.com.tscode.checking.data.local.AppPreferencesDataSource
import br.com.tscode.checking.domain.clientstate.resolvePersistedUserSettings
import br.com.tscode.checking.domain.clientstate.UserSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

// Restores the background engine after device reboot or app update (§23.3-4, T3B.8).
// Handles BOOT_COMPLETED (device restarted) and MY_PACKAGE_REPLACED (app updated).
// Conditions to restart: chave is persisted AND automatic activities is enabled.
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var appPrefs: AppPreferencesDataSource

    private val settingsJson = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        // goAsync() tells the system we need more than the 10-s onReceive window.
        // The coroutine reads DataStore and completes well within that budget.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val chave = appPrefs.chave.first()
                if (chave.isEmpty()) return@launch

                val json = appPrefs.userSettingsJson.first()
                val settingsMap: Map<String, UserSettings?> = runCatching {
                    settingsJson.decodeFromString<Map<String, UserSettings?>>(json)
                }.getOrElse { emptyMap() }

                if (!resolvePersistedUserSettings(settingsMap, chave).automaticActivitiesEnabled) return@launch

                // Start FGS + enqueue WorkManager watchdog (via the single entry point).
                // The FGS registers geofences in its onStartCommand launch block (T3B.9).
                AutoActivityController.start(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
