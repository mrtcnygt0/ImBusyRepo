package com.suanmesgulum.app.di

import com.suanmesgulum.app.data.repository.*
import com.suanmesgulum.app.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository binding'leri için Hilt modülü.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCustomModeRepository(
        impl: CustomModeRepositoryImpl
    ): CustomModeRepository

    @Binds
    @Singleton
    abstract fun bindCallLogRepository(
        impl: CallLogRepositoryImpl
    ): CallLogRepository

    @Binds
    @Singleton
    abstract fun bindCallSessionRepository(
        impl: CallSessionRepositoryImpl
    ): CallSessionRepository

    @Binds
    @Singleton
    abstract fun bindVoicemailRepository(
        impl: VoicemailRepositoryImpl
    ): VoicemailRepository

    @Binds
    @Singleton
    abstract fun bindAssistantSettingsRepository(
        impl: AssistantSettingsRepositoryImpl
    ): AssistantSettingsRepository
}
