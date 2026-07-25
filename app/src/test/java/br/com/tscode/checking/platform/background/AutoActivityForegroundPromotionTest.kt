package br.com.tscode.checking.platform.background

import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoActivityForegroundPromotionTest {
    @Test
    fun `successful promotion allows service work to continue`() {
        var promoteCalls = 0

        val result =
            attemptAutoActivityForegroundPromotion {
                promoteCalls++
            }

        assertTrue(result is AutoActivityForegroundPromotion.Succeeded)
        assertTrue(promoteCalls == 1)
    }

    @Test
    fun `security rejection becomes data and prevents follow-up work`() {
        val expected = SecurityException("foreground location unavailable")
        var followUpStarted = false

        val result =
            attemptAutoActivityForegroundPromotion {
                throw expected
            }
        if (result is AutoActivityForegroundPromotion.Succeeded) {
            followUpStarted = true
        }

        assertTrue(result is AutoActivityForegroundPromotion.Rejected)
        assertSame(expected, (result as AutoActivityForegroundPromotion.Rejected).cause)
        assertFalse(followUpStarted)
    }

    @Test
    fun `other runtime platform rejection is also contained`() {
        val expected = IllegalStateException("foreground start not allowed")

        val result =
            attemptAutoActivityForegroundPromotion {
                throw expected
            }

        assertTrue(result is AutoActivityForegroundPromotion.Rejected)
        assertSame(expected, (result as AutoActivityForegroundPromotion.Rejected).cause)
    }
}
