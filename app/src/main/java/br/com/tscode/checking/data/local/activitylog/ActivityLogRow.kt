package br.com.tscode.checking.data.local.activitylog

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * plan004 §3.3 — one persisted row of the Activities debug log. Enums are stored as their `name` strings
 * (`actor`/`kind`/`severity`) for a simple, stable schema. `atEpochMs` is indexed so the newest-first
 * paged read and the age-based prune are cheap. This DB is isolated from all existing storage.
 */
@Entity(tableName = "activity_log", indices = [Index("atEpochMs")])
data class ActivityLogRow(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val atEpochMs: Long,
    val actor: String,
    val kind: String,
    val severity: String,
    val description: String,
    val location: String?,
)
