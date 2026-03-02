package com.suanmesgulum.app.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.suanmesgulum.app.ImBusyApplication
import com.suanmesgulum.app.R
import com.suanmesgulum.app.service.orchestrator.CallOrchestratorService

/**
 * Telefon durumu BroadcastReceiver — CallScreeningService için yedek mekanizma.
 *
 * v2 Güncelleme:
 * - Gelen aramada 3 butonlu bildirim gösterilir (CallScreeningService ile aynı mantık)
 * - Otomatik yanıtlama modunda direkt CallOrchestratorService başlatılır
 * - IDLE durumunda bildirim kaldırılır
 */
class PhoneStateReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "PhoneStateReceiver"
        private const val NOTIFICATION_TIMEOUT_MS = 15_000L
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val prefs = ServicePreferences(context)

        // Servis kapalıysa hiçbir şey yapma
        if (!prefs.isServiceEnabled) {
            Log.d(TAG, "Servis kapalı, arama yok sayılıyor")
            return
        }

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: ""

        Log.i(TAG, "Telefon durumu değişti: $state, numara: $phoneNumber")

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                if (phoneNumber.isNotBlank()) {
                    prefs.lastIncomingNumber = phoneNumber
                    handleIncomingCall(context, prefs, phoneNumber)
                }
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                Log.d(TAG, "Arama bitti — bildirim kaldırılıyor")
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.cancel(BusyCallScreeningService.NOTIFICATION_ID_INCOMING)
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                Log.d(TAG, "Arama cevaplandı")
            }
        }
    }

    /**
     * Gelen aramayı işle — otomatik yanıtla veya 3 butonlu bildirim göster.
     */
    private fun handleIncomingCall(context: Context, prefs: ServicePreferences, phoneNumber: String) {
        Log.i(TAG, "Gelen arama algılandı: $phoneNumber")

        if (prefs.isAutoAnswer) {
            // Otomatik yanıtlama — CallOrchestratorService başlat
            Log.i(TAG, "Otomatik yanıtlama modu aktif, Asistan başlatılıyor")
            val serviceIntent = Intent(context, CallOrchestratorService::class.java).apply {
                putExtra(BusyCallScreeningService.EXTRA_PHONE_NUMBER, phoneNumber)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } else {
            // Manuel mod — 3 butonlu bildirim göster
            Log.i(TAG, "3 butonlu bildirim gönderiliyor")
            showIncomingCallNotification(context, phoneNumber)
        }
    }

    /**
     * 3 butonlu gelen arama bildirimi göster.
     */
    private fun showIncomingCallNotification(context: Context, phoneNumber: String) {
        // 1. "Asistan Cevaplasın"
        val assistantIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = CallActionReceiver.ACTION_ASSISTANT_ANSWER
            putExtra(BusyCallScreeningService.EXTRA_PHONE_NUMBER, phoneNumber)
        }
        val assistantPendingIntent = PendingIntent.getBroadcast(
            context, 11, assistantIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. "Sessizce Reddet"
        val rejectIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = CallActionReceiver.ACTION_SILENT_REJECT
            putExtra(BusyCallScreeningService.EXTRA_PHONE_NUMBER, phoneNumber)
        }
        val rejectPendingIntent = PendingIntent.getBroadcast(
            context, 12, rejectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. "Yok Say"
        val ignoreIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = CallActionReceiver.ACTION_IGNORE_CALL
            putExtra(BusyCallScreeningService.EXTRA_PHONE_NUMBER, phoneNumber)
        }
        val ignorePendingIntent = PendingIntent.getBroadcast(
            context, 13, ignoreIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ImBusyApplication.CHANNEL_INCOMING_CALL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_incoming_call_title))
            .setContentText(context.getString(R.string.notification_incoming_call_text, phoneNumber))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_call_answer, context.getString(R.string.btn_assistant_answer), assistantPendingIntent)
            .addAction(R.drawable.ic_call_end, context.getString(R.string.btn_silent_reject), rejectPendingIntent)
            .addAction(R.drawable.ic_close, context.getString(R.string.btn_ignore), ignorePendingIntent)
            .setTimeoutAfter(NOTIFICATION_TIMEOUT_MS)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(BusyCallScreeningService.NOTIFICATION_ID_INCOMING, notification)
    }
}
