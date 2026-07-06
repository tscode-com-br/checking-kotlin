package br.com.tscode.checking.presentation.privacy

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tscode.checking.data.local.AppPreferencesDataSource
import br.com.tscode.checking.data.local.SecurePasswordStore
import br.com.tscode.checking.data.local.activitylog.ActivityLog
import br.com.tscode.checking.domain.repository.AuthRepository
import br.com.tscode.checking.i18n.DEFAULT_LANGUAGE
import br.com.tscode.checking.platform.background.AutoActivityController
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the Privacy & Data Protection screen. Exposes the UI language + the current chave (for the DPO
 * e-mail template) and implements LGPD art. 18 (eliminação) locally: [deleteLocalData] wipes every
 * on-device store and signs the user out. Server-side records are untouched (handled via the DPO channel).
 */
@HiltViewModel
class PrivacyViewModel @Inject constructor(
    private val appPrefs: AppPreferencesDataSource,
    private val securePasswordStore: SecurePasswordStore,
    private val activityLog: ActivityLog,
    private val authRepository: AuthRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    val languageFlow = appPrefs.language
        .map { it.ifBlank { DEFAULT_LANGUAGE } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, DEFAULT_LANGUAGE)

    val chaveFlow = appPrefs.chave
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    /** Erases ALL locally stored data and ends the session. Each step is crash-guarded so one failing
     *  store can't leave the wipe half-done silently; onDone runs once the wipe attempt completes. */
    fun deleteLocalData(onDone: () -> Unit) {
        viewModelScope.launch {
            runCatching { AutoActivityController.stop(appContext) } // stop background engine first
            runCatching { authRepository.logout() }                 // clears the encrypted session cookie
            runCatching { activityLog.clear() }                     // local Activities log (Room)
            runCatching { securePasswordStore.clearAll() }          // encrypted saved passwords
            runCatching { appPrefs.clearAll() }                     // DataStore: chave, settings, offline queue, flags
            onDone()
        }
    }
}
