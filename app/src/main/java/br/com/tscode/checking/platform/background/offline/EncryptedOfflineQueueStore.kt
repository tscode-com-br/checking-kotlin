package br.com.tscode.checking.platform.background.offline

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import br.com.tscode.checking.data.local.AppPreferencesDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encrypts the offline check queue at rest (LGPD art. 46). The queue holds PRECISE GPS coordinates
 * captured while offline; unlike the rest of the app's DataStore, those must not sit in cleartext.
 * Backed by EncryptedSharedPreferences (Keystore-backed AES256, the same scheme as saved passwords
 * and session cookies).
 *
 * One-time migration: an install updated from a previous version may still have the queue in the
 * legacy cleartext DataStore key (`pref_pending_checks_json`). On first read we move it into the
 * encrypted store and clear the legacy copy, so offline check-ins captured before the update are not
 * lost. All reads/writes run under the queue's Mutex, so the `migrated` guard needs no locking.
 */
@Singleton
class EncryptedOfflineQueueStore @Inject constructor(
    @ApplicationContext context: Context,
    private val legacyPrefs: AppPreferencesDataSource,
) : OfflineQueueStore {

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "checking_offline_queue",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private var migrated = false

    override suspend fun read(): String {
        migrateLegacyIfNeeded()
        return prefs.getString(KEY, "") ?: ""
    }

    override suspend fun write(json: String) {
        migrated = true // once we own the data there is nothing left to migrate
        prefs.edit().putString(KEY, json).apply()
    }

    private suspend fun migrateLegacyIfNeeded() {
        if (migrated) return
        migrated = true
        if (!prefs.getString(KEY, "").isNullOrEmpty()) return // encrypted store already has data
        val legacy = runCatching { legacyPrefs.pendingChecksJson.first() }.getOrDefault("")
        if (legacy.isNotEmpty()) {
            prefs.edit().putString(KEY, legacy).apply()
            runCatching { legacyPrefs.setPendingChecksJson("") } // drop the cleartext copy
        }
    }

    companion object {
        private const val KEY = "pending_checks_json"
    }
}
