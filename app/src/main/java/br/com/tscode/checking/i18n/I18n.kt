package br.com.tscode.checking.i18n

import android.os.LocaleList
import br.com.tscode.checking.i18n.dictionaries.idDictionary
import br.com.tscode.checking.i18n.dictionaries.enDictionary
import br.com.tscode.checking.i18n.dictionaries.msDictionary
import br.com.tscode.checking.i18n.dictionaries.ptDictionary
import br.com.tscode.checking.i18n.dictionaries.tlDictionary
import br.com.tscode.checking.i18n.dictionaries.zhDictionary

const val DEFAULT_LANGUAGE = "pt"

private val ALL_DICTIONARIES: Map<String, Map<String, Any>> by lazy {
    mapOf(
        "pt" to ptDictionary(),
        "en" to enDictionary(),
        "zh" to zhDictionary(),
        "ms" to msDictionary(),
        "id" to idDictionary(),
        "tl" to tlDictionary(),
    )
}

fun resolveLanguageCode(languageCode: String?, fallback: String? = null): String {
    val supportedCodes = SUPPORTED_LANGUAGES.map { it.code }
    val fallbackWasProvided = fallback != null
    val resolvedFallback = if (fallbackWasProvided) {
        fallback!!.trim().lowercase()
    } else {
        DEFAULT_LANGUAGE
    }

    val normalized = (languageCode ?: "").trim().lowercase()

    if (normalized.isEmpty()) {
        if (resolvedFallback.isEmpty() && fallbackWasProvided) return ""
        return if (supportedCodes.contains(resolvedFallback)) resolvedFallback else (supportedCodes.firstOrNull() ?: DEFAULT_LANGUAGE)
    }

    if (supportedCodes.contains(normalized)) return normalized

    val baseCode = normalized.split(Regex("[-_]")).first()
    val aliasedCode = LANGUAGE_ALIAS_MAP[normalized] ?: LANGUAGE_ALIAS_MAP[baseCode] ?: baseCode
    if (supportedCodes.contains(aliasedCode)) return aliasedCode

    if (resolvedFallback.isEmpty() && fallbackWasProvided) return ""
    return if (supportedCodes.contains(resolvedFallback)) resolvedFallback else (supportedCodes.firstOrNull() ?: DEFAULT_LANGUAGE)
}

fun detectDeviceLanguageCode(): String {
    val locales = LocaleList.getDefault()
    for (i in 0 until locales.size()) {
        val candidate = resolveLanguageCode(locales[i].language, "")
        if (candidate.isNotEmpty()) return candidate
    }
    return resolveLanguageCode(DEFAULT_LANGUAGE)
}

fun getDictionary(languageCode: String): Map<String, Any> {
    val resolved = resolveLanguageCode(languageCode)
    return ALL_DICTIONARIES[resolved] ?: ALL_DICTIONARIES[DEFAULT_LANGUAGE] ?: emptyMap()
}

fun readTranslationValue(source: Map<String, Any>, keyPath: String): Any? {
    val normalized = keyPath.trim()
    if (normalized.isEmpty()) return null
    var current: Any = source
    for (segment in normalized.split('.')) {
        val map = current as? Map<*, *> ?: return null
        current = map[segment] ?: return null
    }
    return current
}

fun interpolateTranslation(template: String, values: Map<String, String>?): String {
    if (values == null) return template
    return Regex("\\{([^}]+)\\}").replace(template) { result ->
        values[result.groupValues[1]] ?: ""
    }
}

fun t(keyPath: String, values: Map<String, String>? = null, lang: String? = null): String {
    val resolvedCode = resolveLanguageCode(lang ?: activeLanguageCode)
    val dictionary = getDictionary(resolvedCode)
    val fallbackDictionary = getDictionary(DEFAULT_LANGUAGE)
    val translatedValue = readTranslationValue(dictionary, keyPath)
    val fallbackValue = if (translatedValue == null) readTranslationValue(fallbackDictionary, keyPath) else translatedValue

    return when (fallbackValue) {
        is String -> interpolateTranslation(fallbackValue, values)
        null -> keyPath
        else -> fallbackValue.toString()
    }
}

var activeLanguageCode: String = DEFAULT_LANGUAGE
    private set

fun setActiveLanguageCode(code: String): String {
    activeLanguageCode = resolveLanguageCode(code)
    return activeLanguageCode
}

fun getLanguageEntry(code: String): LanguageEntry {
    val resolved = resolveLanguageCode(code)
    return SUPPORTED_LANGUAGES.find { it.code == resolved }
        ?: SUPPORTED_LANGUAGES.firstOrNull()
        ?: LanguageEntry(resolved, resolved, resolved, "pt-BR")
}

fun resolveInitialLanguageCode(storedCode: String?, deviceFallback: Boolean = true): String {
    if (!storedCode.isNullOrBlank()) {
        val resolved = resolveLanguageCode(storedCode, "")
        if (resolved.isNotEmpty()) return setActiveLanguageCode(resolved)
    }
    if (deviceFallback) {
        val device = detectDeviceLanguageCode()
        if (device.isNotEmpty()) return setActiveLanguageCode(device)
    }
    return setActiveLanguageCode(DEFAULT_LANGUAGE)
}

// Pure resolution of the effective language for background work (notifications): stored code → device
// locale → pt. Mirrors resolveInitialLanguageCode's precedence but does NOT mutate the global
// activeLanguageCode, so it is safe to call off the UI thread. Used by the FGS/orchestrator so their
// notifications follow the SAME language the UI resolved, instead of always defaulting to pt.
fun resolveEffectiveLanguageCode(storedCode: String?): String {
    if (!storedCode.isNullOrBlank()) {
        val resolved = resolveLanguageCode(storedCode, "")
        if (resolved.isNotEmpty()) return resolved
    }
    val device = detectDeviceLanguageCode()
    if (device.isNotEmpty()) return device
    return DEFAULT_LANGUAGE
}
