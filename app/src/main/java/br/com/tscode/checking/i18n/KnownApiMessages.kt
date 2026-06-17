package br.com.tscode.checking.i18n

import br.com.tscode.checking.domain.checkrules.AUTOMATIC_CHECKOUT_LOCATION
import br.com.tscode.checking.domain.checkrules.AUTOMATIC_UNREGISTERED_CHECKIN_LOCATION
import br.com.tscode.checking.domain.checkrules.MIXED_ZONE_LOCATION
import br.com.tscode.checking.domain.checkrules.isCheckoutZoneLocationName
import br.com.tscode.checking.i18n.dictionaries.ptDictionary

/**
 * Ports `createKnownDictionaryMessageIndex` + `localizeKnownApiMessage` +
 * `localizeKnownLocationLabel` from app.js (§11.4).
 *
 * The server always sends messages in pt-BR.  When the user's active language is
 * something else, this object maps known pt leaf strings back to their i18n key
 * so `t()` can re-emit them in the active language.
 */
object KnownApiMessages {

    private const val DEFAULT_MANUAL_LOCATION = "Escritório Principal"
    private const val ACCURACY_FALLBACK_LOCATION = "Precisao Insuficiente"
    private const val TRANSPORT_CONFLICT_GENERIC =
        "Ja existe uma solicitacao de transporte ativa para essa data."
    private const val TRANSPORT_CONFLICT_PREFIX =
        "Ja existe uma solicitacao de transporte ativa para "

    // Lazily built once: maps every leaf pt string → its dotted key path.
    private val ptIndex: Map<String, String> by lazy {
        val out = mutableMapOf<String, String>()
        buildIndex(ptDictionary(), "", out)
        out
    }

    @Suppress("UNCHECKED_CAST")
    private fun buildIndex(node: Map<String, Any>, prefix: String, out: MutableMap<String, String>) {
        val pfx = if (prefix.isEmpty()) "" else "$prefix."
        node.forEach { (key, value) ->
            val keyPath = "$pfx$key"
            when (value) {
                is String -> out[value] = keyPath
                is Map<*, *> -> buildIndex(value as Map<String, Any>, keyPath, out)
            }
        }
    }

    /**
     * Translate a PT server message to [lang].
     * Returns the original message unchanged for unknown strings or when lang == "pt".
     */
    fun localizeApiMessage(message: String, lang: String = DEFAULT_LANGUAGE): String {
        val raw = message.trim()
        if (raw.isEmpty()) return ""
        if (lang == DEFAULT_LANGUAGE) return raw

        ptIndex[raw]?.let { keyPath -> return t(keyPath, lang = lang) }

        // Transport conflict messages have a dynamic date part not captured by the flat index.
        if (raw == TRANSPORT_CONFLICT_GENERIC) {
            return t("transport.requestBuilder.conflictGeneric", lang = lang)
        }
        if (raw.startsWith(TRANSPORT_CONFLICT_PREFIX) && raw.endsWith(".")) {
            val dateLabel = raw.removePrefix(TRANSPORT_CONFLICT_PREFIX).removeSuffix(".")
            return t(
                "transport.requestBuilder.conflictByDate",
                values = mapOf("serviceDateLabel" to dateLabel),
                lang = lang,
            )
        }

        return raw
    }

    /**
     * Translate a location label: special-cased strings first, then falls through to
     * [localizeApiMessage] for any other known pt message.
     */
    fun localizeLocationLabel(label: String, lang: String = DEFAULT_LANGUAGE): String {
        val normalized = label.trim()
        if (normalized.isEmpty()) return ""
        return when {
            normalized == DEFAULT_MANUAL_LOCATION ->
                t("location.defaultManualLocationLabel", lang = lang)
            normalized == ACCURACY_FALLBACK_LOCATION ->
                t("location.accuracyFallbackManualLocationLabel", lang = lang)
            normalized == AUTOMATIC_CHECKOUT_LOCATION ->
                t("location.outsideWorkplaceLabel", lang = lang)
            normalized == AUTOMATIC_UNREGISTERED_CHECKIN_LOCATION ->
                t("location.unregisteredLocationLabel", lang = lang)
            normalized == MIXED_ZONE_LOCATION ->
                t("location.mixedZoneLabel", lang = lang)
            isCheckoutZoneLocationName(normalized) ->
                t("location.checkoutZoneLabel", lang = lang)
            else -> localizeApiMessage(normalized, lang)
        }
    }
}
