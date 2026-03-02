package com.suanmesgulum.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.suanmesgulum.app.ImBusyApplication
import com.suanmesgulum.app.R
import com.suanmesgulum.app.presentation.MainActivity

/**
 * Foreground Service: Uygulamanın arka planda çalışmaya devam etmesini sağlar.
 *
 * Bu servis aktifken:
 * - Sürekli bir bildirim gösterilir
 * - CallScreeningService çalışır
 * - Gelen aramalar izlenir
 *
 * Kullanıcı ana ekrandaki toggle ile servisi açıp kapatabilir.
 */
class BusyForegroundService : Service() {

    companion object {
        private const val TAG = "BusyForegroundService"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.suanmesgulum.ACTION_START_SERVICE"
        const val ACTION_STOP = "com.suanmesgulum.ACTION_STOP_SERVICE"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Foreground Service oluşturuluyor")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                Log.i(TAG, "Servis durduruluyor")
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                ServicePreferences(this).isServiceEnabled = false
                return START_NOT_STICKY
            }
            else -> {
                Log.i(TAG, "Servis başlatılıyor")
                startForeground(NOTIFICATION_ID, createNotification())
                ServicePreferences(this).isServiceEnabled = true
                return START_STICKY
            }
        }
    }

    /**
     * Foreground service bildirimi oluştur.
     */
    private fun createNotification(): Notification {
        // Bildirime tıklanınca uygulamayı aç
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Servisi Durdur" butonu
        val stopIntent = Intent(this, BusyForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, ImBusyApplication.CHANNEL_SERVICE)
            .setContentTitle(getString(R.string.notification_service_title))
            .setContentText(getString(R.string.notification_service_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .addAction(
                R.drawable.ic_stop,
                getString(R.string.notification_stop_service),
                stopPendingIntent
            )
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Foreground Service yok edildi")
    }
}
