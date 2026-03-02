package com.suanmesgulum.app.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import androidx.core.app.NotificationCompat
import com.suanmesgulum.app.ImBusyApplication
import com.suanmesgulum.app.R
import com.suanmesgulum.app.service.orchestrator.CallOrchestratorService
import kotlinx.coroutines.*

/**
 * CallScreeningService: Gelen aramaları yakalama ve müdahale etme.
 *
 * Güncelleme v2:
 * - Gelen çağrıda kullanıcıya 3 butonlu bildirim gösterilir:
 *   1. "Asistan Cevaplasın" → CallOrchestratorService başlatılır
 *   2. "Sessizce Reddet" → Çağrı reddedilir, log atılır
 *   3. "Yok Say" → Bildirim kapanır, çağrı normal çalar
 * - Otomatik yanıtlama modunda direkt CallOrchestratorService başlatılır
 * - Bildirim 15 saniye sonra otomatik kaybolur
 */
class BusyCallScreeningService : CallScreeningService() {

    companion object {
        private const val TAG = "BusyCallScreening"
        const val NOTIFICATION_ID_INCOMING = 2001
        const val EXTRA_PHONE_NUMBER = "extra_phone_number"
        const val EXTRA_MODE_ID = "extra_mode_id"
        private const val NOTIFICATION_TIMEOUT_MS = 15_000L
    }

    private lateinit var prefs: ServicePreferences

    override fun onCreate() {
        super.onCreate()
        prefs = ServicePreferences(this)
    }

    override fun onScreenCall(callDetails: Call.Details) {
        Log.i(TAG, "Gelen arama algılandı")

        // Servis aktif değilse, aramaya müdahale etme
        if (!prefs.isServiceEnabled) {
            Log.d(TAG, "Servis kapalı, arama geçiriliyor")
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }

        val phoneNumber = callDetails.handle?.schemeSpecificPart ?: "Bilinmeyen"
        Log.i(TAG, "Gelen arama: $phoneNumber")

        // Son gelen numarayı kaydet
        prefs.lastIncomingNumber = phoneNumber

        // Otomatik yanıtlama modunda mı?
        if (prefs.isAutoAnswer) {
            Log.i(TAG, "Otomatik yanıtlama modu aktif — Asistan cevaplıyor")
            startOrchestrator(phoneNumber)

            // Aramayı sessizce bırak (cevaplamayı orchestrator yapacak)
            val response = CallResponse.Builder()
                .setDisallowCall(false)
                .setRejectCall(false)
                .setSilenceCall(true)
                .setSkipCallLog(false)
                .setSkipNotification(true)
                .build()
            respondToCall(callDetails, response)
            return
        }

        // Manuel mod: 3 butonlu bildirim göster
        Log.i(TAG, "Kullanıcıya 3 butonlu bildirim gönderiliyor")
        showIncomingCallNotification(phoneNumber)

        // Aramayı normal çalmaya devam ettir
        val response = CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .setSilenceCall(false)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()
        respondToCall(callDetails, response)

        // 15 saniye sonra bildirimi otomatik kaldır
        CoroutineScope(Dispatchers.Default).launch {
            delay(NOTIFICATION_TIMEOUT_MS)
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(NOTIFICATION_ID_INCOMING)
        }
    }

    /**
     * CallOrchestratorService'i başlat (Asistan cevaplasın).
     */
    private fun startOrchestrator(phoneNumber: String) {
        val intent = Intent(this, CallOrchestratorService::class.java).apply {
            putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    /**
     * 3 butonlu gelen arama bildirimi göster.
     */
    private fun showIncomingCallNotification(phoneNumber: String) {
        // 1. "Asistan Cevaplasın" butonu
        val assistantIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = CallActionReceiver.ACTION_ASSISTANT_ANSWER
            putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
        }
        val assistantPendingIntent = PendingIntent.getBroadcast(
            this, 1, assistantIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. "Sessizce Reddet" butonu
        val rejectIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = CallActionReceiver.ACTION_SILENT_REJECT
            putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
        }
        val rejectPendingIntent = PendingIntent.getBroadcast(
            this, 2, rejectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. "Yok Say" butonu
        val ignoreIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = CallActionReceiver.ACTION_IGNORE_CALL
            putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
        }
        val ignorePendingIntent = PendingIntent.getBroadcast(
            this, 3, ignoreIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, ImBusyApplication.CHANNEL_INCOMING_CALL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notification_incoming_call_title))
            .setContentText(getString(R.string.notification_incoming_call_text, phoneNumber))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_call_answer, getString(R.string.btn_assistant_answer), assistantPendingIntent)
            .addAction(R.drawable.ic_call_end, getString(R.string.btn_silent_reject), rejectPendingIntent)
            .addAction(R.drawable.ic_close, getString(R.string.btn_ignore), ignorePendingIntent)
            .setTimeoutAfter(NOTIFICATION_TIMEOUT_MS)
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID_INCOMING, notification)
    }
}
