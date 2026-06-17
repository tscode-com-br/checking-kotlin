package br.com.tscode.checking.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class CookieJson(
    val name: String,
    val value: String,
    val domain: String,
    val path: String,
    val expiresAt: Long,
    val secure: Boolean,
    val httpOnly: Boolean,
    val hostOnly: Boolean,
)

private fun Cookie.toJson() = CookieJson(
    name = name,
    value = value,
    domain = domain,
    path = path,
    expiresAt = expiresAt,
    secure = secure,
    httpOnly = httpOnly,
    hostOnly = hostOnly,
)

private fun CookieJson.toCookie(): Cookie = Cookie.Builder()
    .name(name)
    .value(value)
    .apply { if (hostOnly) hostOnlyDomain(domain) else domain(domain) }
    .path(path)
    .expiresAt(expiresAt)
    .apply { if (secure) secure() }
    .apply { if (httpOnly) httpOnly() }
    .build()

@Singleton
class PersistentCookieJar @Inject constructor(
    @ApplicationContext private val context: Context,
) : CookieJar {

    private val json = Json { ignoreUnknownKeys = true }

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "checking_cookies",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isEmpty()) return
        val encoded = json.encodeToString(cookies.map { it.toJson() })
        prefs.edit().putString(url.host, encoded).apply()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val raw = prefs.getString(url.host, null) ?: return emptyList()
        return try {
            val now = System.currentTimeMillis()
            json.decodeFromString<List<CookieJson>>(raw)
                .filter { it.expiresAt > now }
                .map { it.toCookie() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
