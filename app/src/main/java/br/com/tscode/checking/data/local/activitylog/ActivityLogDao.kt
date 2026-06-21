package br.com.tscode.checking.data.local.activitylog

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * plan004 §3.3 — Room DAO for the Activities log. Manual paging (no Paging3): `pageNewestFirst`
 * returns blocks of [limit] newest-first; pruning keeps "30 days OR 5,000 rows" via [deleteOlderThan]
 * + [trimToMax]. All calls are suspend (used off the caller's thread).
 */
@Dao
interface ActivityLogDao {
    @Insert
    suspend fun insert(row: ActivityLogRow): Long

    @Query("SELECT * FROM activity_log ORDER BY atEpochMs DESC, id DESC LIMIT :limit OFFSET :offset")
    suspend fun pageNewestFirst(limit: Int, offset: Int): List<ActivityLogRow>

    @Query("SELECT COUNT(*) FROM activity_log")
    suspend fun count(): Int

    @Query("DELETE FROM activity_log WHERE atEpochMs < :epochMs")
    suspend fun deleteOlderThan(epochMs: Long): Int

    @Query(
        "DELETE FROM activity_log WHERE id NOT IN " +
            "(SELECT id FROM activity_log ORDER BY atEpochMs DESC, id DESC LIMIT :max)",
    )
    suspend fun trimToMax(max: Int): Int

    @Query("DELETE FROM activity_log")
    suspend fun clearAll()
}
