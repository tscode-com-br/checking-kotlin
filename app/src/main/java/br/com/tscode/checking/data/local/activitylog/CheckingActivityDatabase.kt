package br.com.tscode.checking.data.local.activitylog

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * plan004 §3.3 — dedicated, isolated Room DB for the Activities debug log. Version 1, brand-new DB file
 * (`checking_activity.db`); does NOT touch any existing storage (DataStore / offline queue stay as-is).
 */
@Database(entities = [ActivityLogRow::class], version = 1, exportSchema = false)
abstract class CheckingActivityDatabase : RoomDatabase() {
    abstract fun activityLogDao(): ActivityLogDao
}
