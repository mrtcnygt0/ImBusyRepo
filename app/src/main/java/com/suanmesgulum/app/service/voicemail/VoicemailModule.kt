package com.suanmesgulum.app.service.voicemail

import android.content.Context
import com.suanmesgulum.app.domain.repository.VoicemailRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Voicemail için Hilt modülü.
 */
@Module
@InstallIn(SingletonComponent::class)
object VoicemailModule {

    @Provides
    @Singleton
    fun provideVoicemailManager(
        @ApplicationContext context: Context,
        voicemailRepository: VoicemailRepository
    ): VoicemailManager {
        return VoicemailManager(context, voicemailRepository)
    }
}
