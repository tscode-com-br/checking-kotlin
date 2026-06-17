package br.com.tscode.checking.di

import android.content.Context
import br.com.tscode.checking.core.time.Clock
import br.com.tscode.checking.core.time.SystemClock
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Platform DI module (§12).
// Provides FusedLocationProviderClient, GeofencingClient, Clock, and connectivity helpers.
// LocationProvider and NetworkMonitor wrappers are wired in Phase 3 (T3.1).
@Module
@InstallIn(SingletonComponent::class)
object PlatformModule {

    @Provides
    @Singleton
    fun provideFusedLocationClient(
        @ApplicationContext context: Context,
    ): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    @Provides
    @Singleton
    fun provideGeofencingClient(
        @ApplicationContext context: Context,
    ): GeofencingClient = LocationServices.getGeofencingClient(context)

    @Provides
    @Singleton
    fun provideClock(): Clock = SystemClock()
}
