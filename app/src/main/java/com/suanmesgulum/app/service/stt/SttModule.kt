package com.suanmesgulum.app.service.stt

import android.content.Context
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * STT (Speech-to-Text) için Hilt modülü.
 */
@Module
@InstallIn(SingletonComponent::class)
object SttModule {

    @Provides
    @Singleton
    fun provideSpeechToTextManager(@ApplicationContext context: Context): SpeechToTextManager {
        return SpeechToTextManager(context)
    }
}
