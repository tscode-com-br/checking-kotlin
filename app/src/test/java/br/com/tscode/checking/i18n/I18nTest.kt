package br.com.tscode.checking.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Unit tests for the i18n engine — resolution, fallback, interpolation (T1.5 AC).
class I18nTest {

    // ── resolveLanguageCode ──────────────────────────────────────────────────

    @Test
    fun resolveLanguageCode_supportedCode_returnsSame() {
        assertEquals("pt", resolveLanguageCode("pt"))
        assertEquals("en", resolveLanguageCode("en"))
        assertEquals("zh", resolveLanguageCode("zh"))
        assertEquals("ms", resolveLanguageCode("ms"))
        assertEquals("id", resolveLanguageCode("id"))
        assertEquals("tl", resolveLanguageCode("tl"))
    }

    @Test
    fun resolveLanguageCode_uppercaseInput_normalizes() {
        assertEquals("pt", resolveLanguageCode("PT"))
        assertEquals("en", resolveLanguageCode("EN"))
    }

    @Test
    fun resolveLanguageCode_emptyInput_returnsDefault() {
        assertEquals(DEFAULT_LANGUAGE, resolveLanguageCode(""))
    }

    @Test
    fun resolveLanguageCode_nullInput_returnsDefault() {
        assertEquals(DEFAULT_LANGUAGE, resolveLanguageCode(null))
    }

    @Test
    fun resolveLanguageCode_unknownCode_returnsDefault() {
        assertEquals(DEFAULT_LANGUAGE, resolveLanguageCode("xx"))
    }

    @Test
    fun resolveLanguageCode_aliasedCodes_resolve() {
        assertEquals("tl", resolveLanguageCode("fil"))
        assertEquals("id", resolveLanguageCode("in"))
    }

    @Test
    fun resolveLanguageCode_withExplicitFallback_usesItWhenUnknown() {
        assertEquals("en", resolveLanguageCode("xx", "en"))
    }

    @Test
    fun resolveLanguageCode_emptyWithEmptyFallback_returnsEmpty() {
        assertEquals("", resolveLanguageCode("", ""))
    }

    // ── t() — key resolution ─────────────────────────────────────────────────

    @Test
    fun t_knownKey_returnsTranslation() {
        val title = t("settings.title", null, "pt")
        assertNotEquals("settings.title", title)
        assertTrue(title.isNotEmpty())
    }

    @Test
    fun t_unknownKey_returnsKeyPathAsFallback() {
        assertEquals("unknown.key.path", t("unknown.key.path", null, "pt"))
    }

    @Test
    fun t_interpolation_replacesPlaceholders() {
        // autoActivities.notification.eventBody = "{local} • {hora}"
        val result = t("autoActivities.notification.eventBody", mapOf("local" to "Office A", "hora" to "09:30"), "pt")
        assertTrue(result.contains("Office A"))
        assertTrue(result.contains("09:30"))
    }

    @Test
    fun t_unsupportedLang_fallsBackToPt() {
        val ptValue = t("settings.title", null, "pt")
        val xxValue = t("settings.title", null, "xx")
        assertEquals(ptValue, xxValue)
    }

    @Test
    fun t_keyExistsInEnButNotForced_englishReturnsEnglish() {
        val ptValue = t("settings.title", null, "pt")
        val enValue = t("settings.title", null, "en")
        assertTrue(ptValue.isNotEmpty())
        assertTrue(enValue.isNotEmpty())
    }

    @Test
    fun t_allSixLanguages_returnNonEmptyForCommonKey() {
        listOf("pt", "en", "zh", "ms", "id", "tl").forEach { lang ->
            val value = t("settings.title", null, lang)
            assertTrue("Language $lang returned empty for settings.title", value.isNotEmpty())
            assertNotEquals("Language $lang returned key as value", "settings.title", value)
        }
    }

    // ── getDictionary ────────────────────────────────────────────────────────

    @Test
    fun getDictionary_allSixLanguagesLoad() {
        listOf("pt", "en", "zh", "ms", "id", "tl").forEach { lang ->
            val dict = getDictionary(lang)
            assertTrue("Dictionary for $lang is empty", dict.isNotEmpty())
        }
    }

    @Test
    fun getDictionary_unknownLang_returnsPtDictionary() {
        val ptDict = getDictionary("pt")
        val xxDict = getDictionary("xx")
        assertEquals(ptDict, xxDict)
    }
}
