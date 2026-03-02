package com.suanmesgulum.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.suanmesgulum.app.data.local.dao.CallLogDao
import com.suanmesgulum.app.data.local.dao.CustomModeDao
import com.suanmesgulum.app.data.local.entity.CallLogEntity
import com.suanmesgulum.app.data.local.entity.CustomModeEntity

/**
 * Uygulamanın Room veritabanı.
 * Modlar ve arama loglarını saklar.
 */
@Database(
    entities = [
        CustomModeEntity::class,
        CallLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customModeDao(): CustomModeDao
    abstract fun callLogDao(): CallLogDao

    companion object {
        const val DATABASE_NAME = "suan_mesgulum_db"
    }
}
