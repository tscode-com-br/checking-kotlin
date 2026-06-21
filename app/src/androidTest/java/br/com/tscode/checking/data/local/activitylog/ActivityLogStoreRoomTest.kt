package br.com.tscode.checking.data.local.activitylog

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.tscode.checking.domain.model.ActivityActor
import br.com.tscode.checking.domain.model.ActivityKind
import br.com.tscode.checking.domain.model.ActivityLogEntry
import br.com.tscode.checking.domain.model.ActivitySeverity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant

// plan004 TP2 — ActivityLog store over REAL in-memory Room: record→page round-trip (newest-first, all
// fields preserved) + age-pruning enforced ON WRITE (30-day cutoff). Complements the fake-DAO
// ActivityLogStoreTest (which proves the prune ARGS) by proving the real Room EFFECT. The 5,000-row cap is
// proven by ActivityLogDaoTest.trimToMax_at5000_keepsNewest5000 + the literal-pin in ActivityLogStoreTest.
class ActivityLogStoreRoomTest {

    private lateinit var db: CheckingActivityDatabase
    private lateinit var store: ActivityLog

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CheckingActivityDatabase::class.java,
        ).build()
        store = ActivityLog(db.activityLogDao())
    }

    @After
    fun tearDown() = db.close()

    private fun entry(
        at: Instant,
        actor: ActivityActor = ActivityActor.SYS,
        kind: ActivityKind = ActivityKind.ACTIVE,
        sev: ActivitySeverity = ActivitySeverity.INFO,
        desc: String = "d",
        loc: String? = null,
    ) = ActivityLogEntry(at = at, actor = actor, kind = kind, severity = sev, description = desc, location = loc)

    @Test
    fun record_then_page_roundTrips_newestFirst_withAllFields() = runBlocking {
        store.record(entry(Instant.parse("2026-06-19T10:00:00Z"), desc = "older"))
        store.record(
            entry(
                Instant.parse("2026-06-19T11:00:00Z"),
                actor = ActivityActor.USER,
                kind = ActivityKind.CHECK_IN,
                sev = ActivitySeverity.SUCCESS,
                desc = "Check-in at Gate 3.",
                loc = "Gate 3",
            ),
        )

        val page = store.page(0, 30)
        assertEquals(2, page.size)
        with(page.first()) { // newest first
            assertEquals("Check-in at Gate 3.", description)
            assertEquals(ActivityActor.USER, actor)
            assertEquals(ActivityKind.CHECK_IN, kind)
            assertEquals(ActivitySeverity.SUCCESS, severity)
            assertEquals("Gate 3", location)
            assertEquals(Instant.parse("2026-06-19T11:00:00Z"), at)
        }
        assertEquals("older", page[1].description)
    }

    @Test
    fun record_prunesEntriesOlderThan30Days_onWrite() = runBlocking {
        val old = Instant.parse("2026-05-01T00:00:00Z")
        store.record(entry(old, desc = "ancient"))
        assertEquals(1, store.count())

        // A new entry > 30 days after the old one → deleteOlderThan(new − 30d) removes the ancient row.
        store.record(entry(old.plusSeconds(31L * 24 * 60 * 60), desc = "fresh"))
        assertEquals(1, store.count())
        assertEquals("fresh", store.page(0, 30).single().description)
    }
}
