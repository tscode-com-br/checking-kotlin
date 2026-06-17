package br.com.tscode.checking.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import br.com.tscode.checking.domain.clientstate.isPasswordLengthValid
import br.com.tscode.checking.domain.clientstate.sanitizeSettingsChave
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePasswordStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "checking_passwords",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun getPassword(chave: String): String {
        val key = sanitizeSettingsChave(chave)
        if (key.length != 4) return ""
        val stored = prefs.getString(key, "") ?: ""
        return if (isPasswordLengthValid(stored)) stored else ""
    }

    fun setPassword(chave: String, password: String) {
        val key = sanitizeSettingsChave(chave)
        if (key.length != 4) return
        if (isPasswordLengthValid(password)) {
            prefs.edit().putString(key, password).apply()
        } else {
            prefs.edit().remove(key).apply()
        }
    }

    fun removePassword(chave: String) {
        val key = sanitizeSettingsChave(chave)
        if (key.length == 4) prefs.edit().remove(key).apply()
    }

    fun getAllPasswords(): Map<String, String> =
        prefs.all.mapNotNull { (k, v) ->
            val key = sanitizeSettingsChave(k)
            val pw = v as? String ?: return@mapNotNull null
            if (key.length == 4 && isPasswordLengthValid(pw)) key to pw else null
        }.toMap()
}
