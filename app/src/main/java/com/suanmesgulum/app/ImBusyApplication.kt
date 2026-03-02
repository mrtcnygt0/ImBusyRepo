package com.suanmesgulum.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

/**
 * Şu an Meşgulüm uygulamasının Application sınıfı.
 * Hilt DI'ı başlatır ve bildirim kanallarını oluşturur.
 */
@HiltAndroidApp
class ImBusyApplication : Application() {

    companion object {
        /** Foreground service bildirimi için kanal */
        const val CHANNEL_SERVICE = "channel_foreground_service"

        /** Gelen arama bildirimi için kanal */
        const val CHANNEL_INCOMING_CALL = "channel_incoming_call"

        /** Genel bildirimler için kanal */
        const val CHANNEL_GENERAL = "channel_general"

        /** Asistan / Orchestrator servisi için kanal */
        const val CHANNEL_ORCHESTRATOR = "channel_orchestrator"

        /** Sesli mesaj bildirimleri için kanal */
        const val CHANNEL_VOICEMAIL = "channel_voicemail"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    /**
     * Bildirim kanallarını oluştur (Android 8.0+).
     */
    private fun createNotificationChannels() {
        val serviceChannel = NotificationChannel(
            CHANNEL_SERVICE,
            getString(R.string.notification_channel_service),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_service_desc)
            setShowBadge(false)
        }

        val incomingCallChannel = NotificationChannel(
            CHANNEL_INCOMING_CALL,
            getString(R.string.notification_channel_incoming_call),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.notification_channel_incoming_call_desc)
            enableVibration(true)
            setShowBadge(true)
        }

        val generalChannel = NotificationChannel(
            CHANNEL_GENERAL,
            getString(R.string.notification_channel_general),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.notification_channel_general_desc)
        }

        val orchestratorChannel = NotificationChannel(
            CHANNEL_ORCHESTRATOR,
            getString(R.string.notification_channel_orchestrator),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_orchestrator_desc)
            setShowBadge(false)
        }

        val voicemailChannel = NotificationChannel(
            CHANNEL_VOICEMAIL,
            getString(R.string.notification_channel_voicemail),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.notification_channel_voicemail_desc)
            enableVibration(true)
            setShowBadge(true)
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
        manager.createNotificationChannel(incomingCallChannel)
        manager.createNotificationChannel(generalChannel)
        manager.createNotificationChannel(orchestratorChannel)
        manager.createNotificationChannel(voicemailChannel)
    }
}
