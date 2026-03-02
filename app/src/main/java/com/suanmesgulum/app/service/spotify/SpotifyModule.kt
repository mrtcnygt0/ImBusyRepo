package com.suanmesgulum.app.service.spotify

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Spotify için Hilt modülü.
 */
@Module
@InstallIn(SingletonComponent::class)
object SpotifyModule {

    @Provides
    @Singleton
    fun provideSpotifyManager(@ApplicationContext context: Context): SpotifyManager {
        return SpotifyManager(context)
    }
}
