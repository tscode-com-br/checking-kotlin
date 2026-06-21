package br.com.tscode.checking.data.local.activitylog

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// plan004 EP4 — Room DAO instrumented tests (in-memory DB). Paging in blocks of 30, age/size pruning,
// newest-first ordering, clear. Uses runBlocking (coroutines-test is not on the androidTest classpath).
class ActivityLogDaoTest {

    private lateinit var db: CheckingActivityDatabase
    private lateinit var dao: ActivityLogDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CheckingActivityDatabase::class.java,
        ).build()
        dao = db.activityLogDao()
    }

    @After
    fun tearDown() = db.close()

    private fun row(at: Long, desc: String = "d") = ActivityLogRow(
        atEpochMs = at, actor = "SYS", kind = "ACTIVE", severity = "INFO", description = desc, location = null,
    )

    @Test
    fun insert_and_count() = runBlocking {
        repeat(3) { dao.insert(row(it.toLong())) }
        assertEquals(3, dao.count())
    }

    @Test
    fun pageNewestFirst_returnsDisjointNewestFirstBlocks() = runBlocking {
        for (i in 1..65) dao.insert(row(i.toLong(), "d$i")) // ascending time → newest = highest atEpochMs
        val page0 = dao.pageNewestFirst(30, 0)
        val page1 = dao.pageNewestFirst(30, 30)
        assertEquals(30, page0.size)
        assertEquals(30, page1.size)
        assertEquals(65L, page0.first().atEpochMs) // newest first
        assertEquals(36L, page0.last().atEpochMs)
        assertEquals(35L, page1.first().atEpochMs)
        val ids0 = page0.map { it.id }.toSet()
        val ids1 = page1.map { it.id }.toSet()
        assertTrue("pages must be disjoint", (ids0 intersect ids1).isEmpty())
    }

    @Test
    fun deleteOlderThan_removesOnlyOld() = runBlocking {
        dao.insert(row(100L)); dao.insert(row(200L)); dao.insert(row(300L))
        dao.deleteOlderThan(200L) // removes atEpochMs < 200 → removes the 100 row
        assertEquals(2, dao.count())
        assertTrue(dao.pageNewestFirst(10, 0).none { it.atEpochMs < 200L })
    }

    @Test
    fun trimToMax_keepsNewestN() = runBlocking {
        for (i in 1..10) dao.insert(row(i.toLong()))
        dao.trimToMax(4)
        assertEquals(4, dao.count())
        assertEquals(listOf(10L, 9L, 8L, 7L), dao.pageNewestFirst(10, 0).map { it.atEpochMs })
    }

    // TP2 — trimToMax(5_000) keeps exactly the newest 5,000 rows (the production retention cap). Done with a
    // single trim call over 5,001 seeded rows (fast), proving the cap at its real value.
    @Test
    fun trimToMax_at5000_keepsNewest5000() = runBlocking {
        for (i in 1..5001) dao.insert(row(i.toLong())) // ascending time → oldest = atEpochMs 1
        dao.trimToMax(5000)
        assertEquals(5000, dao.count())
        val page = dao.pageNewestFirst(5000, 0)
        assertEquals(5001L, page.first().atEpochMs) // newest kept
        assertEquals(2L, page.last().atEpochMs)     // oldest survivor (atEpochMs 1 was dropped)
        assertTrue(page.none { it.atEpochMs == 1L })
    }

    @Test
    fun clearAll_empties() = runBlocking {
        dao.insert(row(1L))
        dao.clearAll()
        assertEquals(0, dao.count())
    }
}
