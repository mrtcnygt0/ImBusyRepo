package com.suanmesgulum.app.service.audio

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Audio için Hilt modülü.
 */
@Module
@InstallIn(SingletonComponent::class)
object AudioModule {

    @Provides
    @Singleton
    fun provideAudioPlayerManager(@ApplicationContext context: Context): AudioPlayerManager {
        return AudioPlayerManager(context)
    }
}
