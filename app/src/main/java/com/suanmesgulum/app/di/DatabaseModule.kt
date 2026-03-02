package com.suanmesgulum.app.di

import android.content.Context
import androidx.room.Room
import com.suanmesgulum.app.data.local.AppDatabase
import com.suanmesgulum.app.data.local.dao.CallLogDao
import com.suanmesgulum.app.data.local.dao.CustomModeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Room Database için Hilt modülü.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideCustomModeDao(database: AppDatabase): CustomModeDao {
        return database.customModeDao()
    }

    @Provides
    fun provideCallLogDao(database: AppDatabase): CallLogDao {
        return database.callLogDao()
    }
}
