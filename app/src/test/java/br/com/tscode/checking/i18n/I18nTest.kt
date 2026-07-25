package br.com.tscode.checking.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    // ── plan004 EP3 regression guard: the 5 new history.* keys must resolve in all 6 languages ─────────
    // EP3 added these keys to every dictionary but shipped no test pinning them; this guards against a
    // future drop from any one dict (which t() would silently mask via the PT fallback at runtime).
    @Test
    fun historyPlan004Keys_resolveInAllSixLanguages() {
        val keys = listOf(
            "history.colActivity", "history.activityCheckin", "history.activityCheckout",
            "history.loadError", "history.retry",
        )
        listOf("pt", "en", "zh", "ms", "id", "tl").forEach { lang ->
            keys.forEach { key ->
                val value = t(key, null, lang)
                assertNotEquals("$key did not resolve for $lang (missing from that dictionary)", key, value)
                assertTrue("$key resolved empty for $lang", value.isNotEmpty())
            }
        }
    }

    // ── plan004 EP8 regression guard: the localized Settings "Activities" row label resolves everywhere ─
    @Test
    fun settingsActivitiesLabel_resolvesInAllSixLanguages() {
        listOf("pt", "en", "zh", "ms", "id", "tl").forEach { lang ->
            val value = t("settings.activitiesLabel", null, lang)
            assertNotEquals("settings.activitiesLabel did not resolve for $lang", "settings.activitiesLabel", value)
            assertTrue("settings.activitiesLabel resolved empty for $lang", value.isNotEmpty())
        }
    }

    @Test
    fun noProjectMembershipMessage_isDefinedInAllSixLanguages() {
        val expected =
            mapOf(
                "pt" to "O usuário não está cadastrado em nenhum projeto.",
                "en" to "The user is not registered in any project.",
                "zh" to "该用户未注册到任何项目。",
                "ms" to "Pengguna tidak didaftarkan dalam mana-mana projek.",
                "id" to "Pengguna tidak terdaftar di proyek mana pun.",
                "tl" to "Hindi nakarehistro ang user sa anumang proyekto.",
            )

        expected.forEach { (lang, message) ->
            assertEquals(message, t("projects.noProjectMembership", null, lang))
        }
    }

    @Test
    fun lowAccuracyRetryNotification_isLocalizedInAllSixLanguages() {
        val expected = mapOf(
            "pt" to listOf(
                "Check-in - Falha!",
                "Check-out - Falha!",
                "Atividade automática - Falha!",
                "Baixa Precisão. Tentará novamente.",
            ),
            "en" to listOf(
                "Check-in - Failed!",
                "Check-out - Failed!",
                "Automatic activity - Failed!",
                "Low accuracy. Will try again.",
            ),
            "id" to listOf(
                "Check-in - Gagal!",
                "Check-out - Gagal!",
                "Aktivitas otomatis - Gagal!",
                "Akurasi rendah. Akan mencoba lagi.",
            ),
            "ms" to listOf(
                "Check-in - Gagal!",
                "Check-out - Gagal!",
                "Aktiviti automatik - Gagal!",
                "Ketepatan rendah. Akan mencuba lagi.",
            ),
            "tl" to listOf(
                "Check-in - Nabigo!",
                "Check-out - Nabigo!",
                "Awtomatikong aktibidad - Nabigo!",
                "Mababa ang katumpakan. Susubukan muli.",
            ),
            "zh" to listOf(
                "签到 - 失败！",
                "签退 - 失败！",
                "自动活动 - 失败！",
                "定位精度低。将再次尝试。",
            ),
        )
        val keys = listOf(
            "lowAccuracyRetry.checkinTitle",
            "lowAccuracyRetry.checkoutTitle",
            "lowAccuracyRetry.automaticActivityTitle",
            "lowAccuracyRetry.body",
        )

        expected.forEach { (lang, values) ->
            keys.zip(values).forEach { (key, value) ->
                assertEquals("$key for $lang", value, t(key, lang = lang))
            }
        }
    }

    // ── U2 regression guard: a manual section title must NEVER carry its own number prefix ────────────
    // ManualScreen.ManualSection renders the number once (the bold `index`); if a title also began with
    // "N. " it would reproduce the old "N. N." double-numbering. Guard all 16 sections × 6 languages.
    @Test
    fun manualSectionTitles_doNotEmbedNumberPrefix() {
        val sections = listOf(
            "overview", "authFlow", "userRegistration", "passwordRegistration", "login", "attendance",
            "projectSelection", "location", "automaticActivities", "transport", "passwordChange",
            "settings", "support", "faq", "scheduledPause", "accident",
        )
        val numberPrefix = Regex("""^\s*\d+\.""")
        listOf("pt", "en", "zh", "ms", "id", "tl").forEach { lang ->
            sections.forEach { sec ->
                val key = "manual.sections.$sec.title"
                val title = t(key, null, lang)
                assertNotEquals("$key did not resolve for $lang", key, title)
                assertFalse(
                    "$key for $lang must not start with a number prefix (would cause \"N. N.\"): '$title'",
                    numberPrefix.containsMatchIn(title),
                )
            }
        }
    }
}
