package br.com.tscode.checking.di

import android.content.Context
import androidx.room.Room
import br.com.tscode.checking.data.local.activitylog.ActivityLogDao
import br.com.tscode.checking.data.local.activitylog.CheckingActivityDatabase
import br.com.tscode.checking.platform.activitylog.ApplicationScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

// plan004 §3.3 — provides the dedicated, isolated Room DB + DAO for the Activities log, plus the
// application-lifetime coroutine scope ActivityLogger uses to persist off the caller's thread.
@Module
@InstallIn(SingletonComponent::class)
object ActivityLogModule {

    @Provides
    @Singleton
    fun provideActivityDatabase(@ApplicationContext context: Context): CheckingActivityDatabase =
        Room.databaseBuilder(
            context,
            CheckingActivityDatabase::class.java,
            "checking_activity.db",
        ).build()

    @Provides
    fun provideActivityLogDao(db: CheckingActivityDatabase): ActivityLogDao = db.activityLogDao()

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}
