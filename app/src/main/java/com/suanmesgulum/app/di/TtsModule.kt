package com.suanmesgulum.app.di

import android.content.Context
import com.suanmesgulum.app.tts.AndroidTtsEngine
import com.suanmesgulum.app.tts.ChatterboxTtsEngine
import com.suanmesgulum.app.tts.TtsEngine
import com.suanmesgulum.app.tts.TtsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

/**
 * TTS motorları için Hilt modülü.
 */
@Module
@InstallIn(SingletonComponent::class)
object TtsModule {

    @Provides
    @Singleton
    @Named("android_tts")
    fun provideAndroidTtsEngine(@ApplicationContext context: Context): TtsEngine {
        return AndroidTtsEngine(context)
    }

    @Provides
    @Singleton
    @Named("chatterbox_tts")
    fun provideChatterboxTtsEngine(@ApplicationContext context: Context): TtsEngine {
        return ChatterboxTtsEngine(context)
    }

    @Provides
    @Singleton
    fun provideTtsManager(
        @Named("android_tts") androidTts: TtsEngine,
        @Named("chatterbox_tts") chatterboxTts: TtsEngine
    ): TtsManager {
        return TtsManager(androidTts, chatterboxTts)
    }
}
