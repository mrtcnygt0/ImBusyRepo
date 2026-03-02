package com.suanmesgulum.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.suanmesgulum.app.service.orchestrator.CallOrchestratorService

/**
 * BroadcastReceiver: Bildirim butonlarından gelen aksiyonları işler.
 *
 * v2 Desteklenen aksiyonlar:
 * - ACTION_ASSISTANT_ANSWER: Asistan cevaplasın → CallOrchestratorService başlat
 * - ACTION_SILENT_REJECT: Aramayı sessizce reddet
 * - ACTION_IGNORE_CALL: Yok say — bildirimi kaldır
 * - ACTION_USE_MODE: (Eski) Seçilen modu kullanarak aramayı yanıtla
 * - ACTION_STOP_SERVICE: Foreground servisi durdur
 */
class CallActionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "CallActionReceiver"
        const val ACTION_USE_MODE = "com.suanmesgulum.ACTION_USE_MODE"
        const val ACTION_SILENT_REJECT = "com.suanmesgulum.ACTION_SILENT_REJECT"
        const val ACTION_STOP_SERVICE = "com.suanmesgulum.ACTION_STOP_SERVICE"
        const val ACTION_ASSISTANT_ANSWER = "com.suanmesgulum.ACTION_ASSISTANT_ANSWER"
        const val ACTION_IGNORE_CALL = "com.suanmesgulum.ACTION_IGNORE_CALL"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val phoneNumber = intent.getStringExtra(BusyCallScreeningService.EXTRA_PHONE_NUMBER) ?: ""

        Log.i(TAG, "Aksiyon alındı: $action, numara: $phoneNumber")

        when (action) {
            ACTION_ASSISTANT_ANSWER -> {
                handleAssistantAnswer(context, phoneNumber)
            }
            ACTION_USE_MODE -> {
                val modeId = intent.getLongExtra(BusyCallScreeningService.EXTRA_MODE_ID, -1L)
                if (modeId != -1L) {
                    handleUseMode(context, phoneNumber, modeId)
                } else {
                    Log.e(TAG, "Geçersiz mod ID'si")
                }
            }
            ACTION_SILENT_REJECT -> {
                handleSilentReject(context, phoneNumber)
            }
            ACTION_IGNORE_CALL -> {
                Log.i(TAG, "Arama yok sayıldı: $phoneNumber")
                // Sadece bildirimi kapat, arama normal çalmaya devam eder
            }
            ACTION_STOP_SERVICE -> {
                handleStopService(context)
            }
        }

        // Gelen arama bildirimini kaldır
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as android.app.NotificationManager
        notificationManager.cancel(BusyCallScreeningService.NOTIFICATION_ID_INCOMING)
    }

    /**
     * Asistan cevaplasın: CallOrchestratorService başlat.
     */
    private fun handleAssistantAnswer(context: Context, phoneNumber: String) {
        Log.i(TAG, "Asistan cevaplıyor: $phoneNumber")

        val serviceIntent = Intent(context, CallOrchestratorService::class.java).apply {
            putExtra(BusyCallScreeningService.EXTRA_PHONE_NUMBER, phoneNumber)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    /**
     * Seçilen modu kullanarak TTS oynatma servisini başlat (eski mod).
     */
    private fun handleUseMode(context: Context, phoneNumber: String, modeId: Long) {
        Log.i(TAG, "Mod kullanılıyor: $modeId, numara: $phoneNumber")

        val serviceIntent = Intent(context, TtsPlaybackService::class.java).apply {
            putExtra(BusyCallScreeningService.EXTRA_PHONE_NUMBER, phoneNumber)
            putExtra(BusyCallScreeningService.EXTRA_MODE_ID, modeId)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    /**
     * Aramayı sessizce reddet (TTS okumadan).
     */
    private fun handleSilentReject(context: Context, phoneNumber: String) {
        Log.i(TAG, "Sessiz reddetme: $phoneNumber")

        try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE)
                as android.telecom.TelecomManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                @Suppress("DEPRECATION")
                telecomManager.endCall()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Arama sonlandırma izni yok", e)
        } catch (e: Exception) {
            Log.e(TAG, "Arama sonlandırma hatası", e)
        }
    }

    /**
     * Foreground servisi durdur.
     */
    private fun handleStopService(context: Context) {
        Log.i(TAG, "Servis durduruluyor")
        val stopIntent = Intent(context, BusyForegroundService::class.java).apply {
            action = BusyForegroundService.ACTION_STOP
        }
        context.startService(stopIntent)
    }
}
