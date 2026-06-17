package br.com.tscode.checking.domain.clientstate

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Unit tests for ClientStateFunctions.kt and PasswordRules.kt — ported from web-client-state.js.
// Vectors mirror the JS test cases to guarantee parity (T1.3 AC).
class ClientStateFunctionsTest {

    // ── sanitizeSettingsChave ────────────────────────────────────────────────

    @Test
    fun sanitizeChave_uppercasesAndStripsNonAlphanumeric() {
        assertEquals("AB12", sanitizeSettingsChave("ab12"))
    }

    @Test
    fun sanitizeChave_stripsSpecialChars() {
        assertEquals("AB12", sanitizeSettingsChave("AB-12!"))
    }

    @Test
    fun sanitizeChave_truncatesToFour() {
        assertEquals("ABCD", sanitizeSettingsChave("ABCDEFGH"))
    }

    @Test
    fun sanitizeChave_nullReturnsEmpty() {
        assertEquals("", sanitizeSettingsChave(null))
    }

    @Test
    fun sanitizeChave_emptyReturnsEmpty() {
        assertEquals("", sanitizeSettingsChave(""))
    }

    @Test
    fun sanitizeChave_mixedCaseAndSymbols() {
        assertEquals("A1B2", sanitizeSettingsChave("a1-b2#"))
    }

    // ── splitNotificationMessage ─────────────────────────────────────────────

    @Test
    fun splitMessage_shortString_entirelyPrimary() {
        val result = splitNotificationMessage("Hello")
        assertEquals("Hello", result.primary)
        assertEquals("", result.secondary)
    }

    @Test
    fun splitMessage_nullInput_returnsEmpty() {
        val result = splitNotificationMessage(null)
        assertEquals("", result.primary)
        assertEquals("", result.secondary)
    }

    @Test
    fun splitMessage_multiLineInput_firstLineIsPrimary() {
        val result = splitNotificationMessage("Line one\nLine two\nLine three")
        assertEquals("Line one", result.primary)
        assertEquals("Line two Line three", result.secondary)
    }

    @Test
    fun splitMessage_longSingleLine_splitAtWordBoundary() {
        // 62-char limit — build a string >62 chars with a word break before the limit
        val msg = "This is a moderately long message that exceeds the sixty-two character limit"
        val result = splitNotificationMessage(msg)
        assertTrue(result.primary.length <= 62)
        assertTrue(result.primary.isNotEmpty())
        assertTrue(result.secondary.isNotEmpty())
        // Reconstructed text matches original (no chars lost, just split)
        assertEquals(msg.trim(), (result.primary + " " + result.secondary).trim())
    }

    @Test
    fun splitMessage_emptyString_returnsEmpty() {
        val result = splitNotificationMessage("")
        assertEquals("", result.primary)
        assertEquals("", result.secondary)
    }

    @Test
    fun splitMessage_exactlyAtLimit_nothingInSecondary() {
        val msg = "A".repeat(62)
        val result = splitNotificationMessage(msg)
        assertEquals(msg, result.primary)
        assertEquals("", result.secondary)
    }

    // ── normalizeProjectValue ────────────────────────────────────────────────

    @Test
    fun normalizeProject_validProject_returnsUppercase() {
        assertEquals("ALPHA", normalizeProjectValue("alpha", listOf("ALPHA", "BETA"), "ALPHA"))
    }

    @Test
    fun normalizeProject_unknownProject_returnsFallback() {
        assertEquals("ALPHA", normalizeProjectValue("GAMMA", listOf("ALPHA", "BETA"), "ALPHA"))
    }

    @Test
    fun normalizeProject_nullInput_returnsFallback() {
        assertEquals("ALPHA", normalizeProjectValue(null, listOf("ALPHA"), "ALPHA"))
    }

    // ── PasswordRules ────────────────────────────────────────────────────────

    @Test
    fun passwordLength_validRange_returnsTrue() {
        assertTrue(isPasswordLengthValid("abc"))
        assertTrue(isPasswordLengthValid("abcdefghij")) // exactly 10
    }

    @Test
    fun passwordLength_tooShort_returnsFalse() {
        assertFalse(isPasswordLengthValid("ab")) // 2 chars
        assertFalse(isPasswordLengthValid(null))
        assertFalse(isPasswordLengthValid(""))
    }

    @Test
    fun passwordLength_tooLong_returnsFalse() {
        assertFalse(isPasswordLengthValid("abcdefghijk")) // 11 chars
    }

    @Test
    fun passwordVerification_nonEmptyUpToTen_returnsTrue() {
        assertTrue(isPasswordVerificationInputValid("x"))
        assertTrue(isPasswordVerificationInputValid("abcdefghij"))
    }

    @Test
    fun passwordVerification_emptyOrNull_returnsFalse() {
        assertFalse(isPasswordVerificationInputValid(""))
        assertFalse(isPasswordVerificationInputValid(null))
    }
}
