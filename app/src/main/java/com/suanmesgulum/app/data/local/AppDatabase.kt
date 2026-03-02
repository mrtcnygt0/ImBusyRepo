package com.suanmesgulum.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.suanmesgulum.app.data.local.dao.*
import com.suanmesgulum.app.data.local.entity.*

/**
 * Uygulamanın Room veritabanı.
 * Modlar, arama logları, oturumlar, mesajlar, sesli mesajlar,
 * Spotify şarkıları ve asistan ayarlarını saklar.
 */
@Database(
    entities = [
        CustomModeEntity::class,
        CallLogEntity::class,
        CallSessionEntity::class,
        CallMessageEntity::class,
        VoicemailEntity::class,
        SpotifyTrackEntity::class,
        AssistantSettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customModeDao(): CustomModeDao
    abstract fun callLogDao(): CallLogDao
    abstract fun callSessionDao(): CallSessionDao
    abstract fun callMessageDao(): CallMessageDao
    abstract fun voicemailDao(): VoicemailDao
    abstract fun spotifyTrackDao(): SpotifyTrackDao
    abstract fun assistantSettingsDao(): AssistantSettingsDao

    companion object {
        const val DATABASE_NAME = "suan_mesgulum_db"

        /**
         * Veritabanı v1 → v2 migration.
         * Yeni tablolar: call_sessions, call_messages, voicemails,
         * spotify_tracks, assistant_settings
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // call_sessions tablosu
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `call_sessions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `callLogId` INTEGER NOT NULL DEFAULT 0,
                        `startTime` INTEGER NOT NULL,
                        `endTime` INTEGER NOT NULL DEFAULT 0,
                        `recordingPath` TEXT,
                        `transcript` TEXT,
                        `status` TEXT NOT NULL DEFAULT 'DEVAM_EDIYOR',
                        FOREIGN KEY(`callLogId`) REFERENCES `call_logs`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_call_sessions_callLogId` ON `call_sessions` (`callLogId`)")

                // call_messages tablosu
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `call_messages` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sessionId` INTEGER NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `speaker` TEXT NOT NULL,
                        `messageText` TEXT,
                        `audioFilePath` TEXT,
                        FOREIGN KEY(`sessionId`) REFERENCES `call_sessions`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_call_messages_sessionId` ON `call_messages` (`sessionId`)")

                // voicemails tablosu
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `voicemails` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `callerNumber` TEXT NOT NULL,
                        `callerName` TEXT,
                        `receivedTime` INTEGER NOT NULL,
                        `duration` INTEGER NOT NULL DEFAULT 0,
                        `audioFilePath` TEXT NOT NULL,
                        `transcript` TEXT,
                        `isListened` INTEGER NOT NULL DEFAULT 0,
                        `isArchived` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                // spotify_tracks tablosu
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `spotify_tracks` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `name` TEXT NOT NULL,
                        `artist` TEXT NOT NULL,
                        `albumArtUrl` TEXT,
                        `isDefault` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                // assistant_settings tablosu
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `assistant_settings` (
                        `id` INTEGER PRIMARY KEY NOT NULL DEFAULT 1,
                        `assistantName` TEXT NOT NULL DEFAULT 'Asistan',
                        `personality` TEXT NOT NULL DEFAULT 'Resmi',
                        `defaultGreetingMessageId` INTEGER NOT NULL DEFAULT -1,
                        `defaultVoiceId` TEXT NOT NULL DEFAULT 'default',
                        `defaultLanguage` TEXT NOT NULL DEFAULT 'tr',
                        `spotifyConnected` INTEGER NOT NULL DEFAULT 0,
                        `spotifyPlaylistId` TEXT,
                        `infoGatheringMessage` TEXT NOT NULL DEFAULT 'Size nasıl yardımcı olabilirim?',
                        `farewellMessage` TEXT NOT NULL DEFAULT 'İlginiz için teşekkürler, iyi günler.',
                        `voicemailPromptMessage` TEXT NOT NULL DEFAULT 'Bir mesaj bırakmak ister misiniz? Lütfen mesajınızı bıptan sonra söyleyin.'
                    )
                """.trimIndent())

                // Varsayılan asistan ayarlarını ekle
                db.execSQL("INSERT OR IGNORE INTO `assistant_settings` (`id`) VALUES (1)")
            }
        }
    }
}
