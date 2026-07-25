package br.com.tscode.checking.platform.background

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AutoActivityServiceStartPolicyTest {
    @Test
    fun `visible Android 14 start keeps existing fine-only degraded mode`() {
        assertNull(
            autoActivityServiceStartBlockReason(
                prerequisites = prerequisites(sdkInt = 34, backgroundLocationGranted = false),
                origin = AutoActivityServiceStartOrigin.USER_VISIBLE,
            ),
        )
    }

    @Test
    fun `Android 14 background origins require background location`() {
        val backgroundOrigins =
            AutoActivityServiceStartOrigin.entries.filter {
                it.requiresBackgroundLocation
            }

        backgroundOrigins.forEach { origin ->
            assertEquals(
                "origin=$origin",
                AutoActivityServiceStartBlockReason.MISSING_BACKGROUND_LOCATION,
                autoActivityServiceStartBlockReason(
                    prerequisites = prerequisites(sdkInt = 34, backgroundLocationGranted = false),
                    origin = origin,
                ),
            )
        }
    }

    @Test
    fun `Android 14 background origins remain eligible with all-time location`() {
        AutoActivityServiceStartOrigin.entries
            .filter { it.requiresBackgroundLocation }
            .forEach { origin ->
                assertNull(
                    "origin=$origin",
                    autoActivityServiceStartBlockReason(
                        prerequisites = prerequisites(sdkInt = 34, backgroundLocationGranted = true),
                        origin = origin,
                    ),
                )
            }
    }

    @Test
    fun `Android 16 keeps enforcing background location on background restart`() {
        assertEquals(
            AutoActivityServiceStartBlockReason.MISSING_BACKGROUND_LOCATION,
            autoActivityServiceStartBlockReason(
                prerequisites = prerequisites(sdkInt = 36, backgroundLocationGranted = false),
                origin = AutoActivityServiceStartOrigin.SYSTEM_RESTART,
            ),
        )
    }

    @Test
    fun `Android 13 retains previous background start eligibility`() {
        assertNull(
            autoActivityServiceStartBlockReason(
                prerequisites = prerequisites(sdkInt = 33, backgroundLocationGranted = false),
                origin = AutoActivityServiceStartOrigin.WATCHDOG,
            ),
        )
    }

    @Test
    fun `Android 10 through 13 warn without changing prior start eligibility`() {
        (29..33).forEach { sdkInt ->
            val prerequisites =
                prerequisites(sdkInt = sdkInt, backgroundLocationGranted = false)

            assertNull(
                "sdk=$sdkInt",
                autoActivityServiceStartBlockReason(
                    prerequisites = prerequisites,
                    origin = AutoActivityServiceStartOrigin.WATCHDOG,
                ),
            )
            assertEquals(
                "sdk=$sdkInt",
                true,
                shouldWarnBackgroundLocationRequired(
                    prerequisites = prerequisites,
                    origin = AutoActivityServiceStartOrigin.WATCHDOG,
                ),
            )
        }
    }

    @Test
    fun `background warning is limited to a new background recreation with precise location`() {
        val missingBackground =
            prerequisites(sdkInt = 34, backgroundLocationGranted = false)

        assertEquals(
            false,
            shouldWarnBackgroundLocationRequired(
                prerequisites = missingBackground,
                origin = AutoActivityServiceStartOrigin.USER_VISIBLE,
            ),
        )
        assertEquals(
            false,
            shouldWarnBackgroundLocationRequired(
                prerequisites = missingBackground,
                origin = AutoActivityServiceStartOrigin.WATCHDOG,
                serviceAlreadyRunning = true,
            ),
        )
        assertEquals(
            false,
            shouldWarnBackgroundLocationRequired(
                prerequisites =
                    prerequisites(
                        sdkInt = 34,
                        fineLocationGranted = false,
                        backgroundLocationGranted = false,
                    ),
                origin = AutoActivityServiceStartOrigin.WATCHDOG,
            ),
        )
        assertEquals(
            false,
            shouldWarnBackgroundLocationRequired(
                prerequisites =
                    prerequisites(
                        sdkInt = 34,
                        backgroundLocationGranted = false,
                        locationEnabled = false,
                    ),
                origin = AutoActivityServiceStartOrigin.WATCHDOG,
            ),
        )
        assertEquals(
            false,
            shouldWarnBackgroundLocationRequired(
                prerequisites = prerequisites(sdkInt = 28),
                origin = AutoActivityServiceStartOrigin.WATCHDOG,
            ),
        )
    }

    @Test
    fun `already promoted service accepts refresh command despite changed prerequisites`() {
        assertNull(
            autoActivityServiceStartBlockReason(
                prerequisites =
                    prerequisites(
                        fineLocationGranted = false,
                        backgroundLocationGranted = false,
                        locationEnabled = false,
                    ),
                origin = AutoActivityServiceStartOrigin.GEOFENCE,
                serviceAlreadyRunning = true,
            ),
        )
    }

    @Test
    fun `missing precise location blocks every origin`() {
        AutoActivityServiceStartOrigin.entries.forEach { origin ->
            assertEquals(
                "origin=$origin",
                AutoActivityServiceStartBlockReason.MISSING_FINE_LOCATION,
                autoActivityServiceStartBlockReason(
                    prerequisites =
                        prerequisites(
                            fineLocationGranted = false,
                            backgroundLocationGranted = false,
                        ),
                    origin = origin,
                ),
            )
        }
    }

    @Test
    fun `disabled device location blocks every origin on Android 14`() {
        AutoActivityServiceStartOrigin.entries.forEach { origin ->
            assertEquals(
                "origin=$origin",
                AutoActivityServiceStartBlockReason.LOCATION_DISABLED,
                autoActivityServiceStartBlockReason(
                    prerequisites = prerequisites(locationEnabled = false),
                    origin = origin,
                ),
            )
        }
    }

    @Test
    fun `Android 13 retains prior eligibility while device location is disabled`() {
        assertNull(
            autoActivityServiceStartBlockReason(
                prerequisites = prerequisites(sdkInt = 33, locationEnabled = false),
                origin = AutoActivityServiceStartOrigin.WATCHDOG,
            ),
        )
    }

    @Test
    fun `missing or unknown origin is treated as background system restart`() {
        assertEquals(
            AutoActivityServiceStartOrigin.SYSTEM_RESTART,
            AutoActivityServiceStartOrigin.fromWireValue(null),
        )
        assertEquals(
            AutoActivityServiceStartOrigin.SYSTEM_RESTART,
            AutoActivityServiceStartOrigin.fromWireValue("unknown"),
        )
        AutoActivityServiceStartOrigin.entries.forEach { origin ->
            assertEquals(
                origin,
                AutoActivityServiceStartOrigin.fromWireValue(origin.wireValue),
            )
        }
    }

    private fun prerequisites(
        sdkInt: Int = 34,
        fineLocationGranted: Boolean = true,
        backgroundLocationGranted: Boolean = true,
        locationEnabled: Boolean = true,
    ): AutoActivityServicePrerequisites =
        AutoActivityServicePrerequisites(
            sdkInt = sdkInt,
            fineLocationGranted = fineLocationGranted,
            backgroundLocationGranted = backgroundLocationGranted,
            locationEnabled = locationEnabled,
        )
}
