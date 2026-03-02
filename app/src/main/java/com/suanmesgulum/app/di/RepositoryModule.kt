package com.suanmesgulum.app.di

import com.suanmesgulum.app.data.repository.CallLogRepositoryImpl
import com.suanmesgulum.app.data.repository.CustomModeRepositoryImpl
import com.suanmesgulum.app.domain.repository.CallLogRepository
import com.suanmesgulum.app.domain.repository.CustomModeRepository
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
}
