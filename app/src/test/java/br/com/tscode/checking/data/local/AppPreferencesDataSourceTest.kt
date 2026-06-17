package br.com.tscode.checking.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class AppPreferencesDataSourceTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val scope = TestScope(dispatcher)
    private lateinit var prefsFile: File
    private lateinit var source: AppPreferencesDataSource

    @Before
    fun setUp() {
        prefsFile = File.createTempFile("test_app_prefs", ".preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { prefsFile },
        )
        source = AppPreferencesDataSource(dataStore)
    }

    @After
    fun tearDown() {
        prefsFile.delete()
        File("${prefsFile.path}.lock").delete()
    }

    @Test
    fun `language defaults to empty and round-trips`() = scope.runTest {
        source.language.test {
            assertEquals("", awaitItem())
            source.setLanguage("zh")
            assertEquals("zh", awaitItem())
            source.setLanguage("en")
            assertEquals("en", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `chave defaults to empty and round-trips`() = scope.runTest {
        source.chave.test {
            assertEquals("", awaitItem())
            source.setChave("HR70")
            assertEquals("HR70", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `userSettingsJson round-trips raw JSON string`() = scope.runTest {
        val json = """{"HR70":{"projects":["PROJ1"],"activeProject":"PROJ1","automaticActivitiesEnabled":true}}"""
        source.userSettingsJson.test {
            assertEquals("", awaitItem())
            source.setUserSettingsJson(json)
            assertEquals(json, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `transportLocalJson round-trips`() = scope.runTest {
        val json = """{"HR70":{"dismissed":["req-1"],"realized":[]}}"""
        source.transportLocalJson.test {
            assertEquals("", awaitItem())
            source.setTransportLocalJson(json)
            assertEquals(json, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `flag defaults to false and round-trips`() = scope.runTest {
        source.getFlag("location_hint_shown").test {
            assertFalse(awaitItem())
            source.setFlag("location_hint_shown", true)
            assertTrue(awaitItem())
            source.setFlag("location_hint_shown", false)
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `different flag names are independent`() = scope.runTest {
        source.setFlag("flag_a", true)
        source.setFlag("flag_b", false)

        source.getFlag("flag_a").test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        source.getFlag("flag_b").test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `overwriting chave replaces previous value`() = scope.runTest {
        source.setChave("AA00")
        source.setChave("BB11")
        source.chave.test {
            assertEquals("BB11", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
