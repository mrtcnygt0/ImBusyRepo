package com.suanmesgulum.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import com.suanmesgulum.app.presentation.incomingcall.IncomingCallActivity

/**
 * Telefon durumu BroadcastReceiver — CallScreeningService için yedek mekanizma.
 *
 * CallScreeningService'in çalışabilmesi için kullanıcının uygulamayı
 * "varsayılan çağrı tarama uygulaması" olarak onaylaması gerekir.
 * Bu onay verilmemişse veya cihaz desteklemiyorsa, bu receiver
 * READ_PHONE_STATE izniyle gelen aramaları algılar.
 *
 * Akış:
 * 1. PHONE_STATE broadcast'i alınır
 * 2. Durum RINGING ise ve servis aktifse:
 *    a) Otomatik yanıtlama açıksa → TtsPlaybackService başlatılır
 *    b) Manuel modda → IncomingCallActivity tam ekran açılır
 * 3. Durum IDLE olduğunda IncomingCallActivity kapatılır
 */
class PhoneStateReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "PhoneStateReceiver"
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
                Log.d(TAG, "Arama bitti")
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                Log.d(TAG, "Arama cevaplandı")
            }
        }
    }

    /**
     * Gelen aramayı işle — otomatik yanıtla veya tam ekran arayüz aç.
     */
    private fun handleIncomingCall(context: Context, prefs: ServicePreferences, phoneNumber: String) {
        Log.i(TAG, "Gelen arama algılandı: $phoneNumber")

        if (prefs.isAutoAnswer && prefs.defaultModeId != -1L) {
            // Otomatik yanıtlama — TtsPlaybackService başlat
            Log.i(TAG, "Otomatik yanıtlama modu aktif, TTS başlatılıyor")
            val serviceIntent = Intent(context, TtsPlaybackService::class.java).apply {
                putExtra(BusyCallScreeningService.EXTRA_PHONE_NUMBER, phoneNumber)
                putExtra(BusyCallScreeningService.EXTRA_MODE_ID, prefs.defaultModeId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } else {
            // Manuel mod — tam ekran arama arayüzünü aç
            Log.i(TAG, "Tam ekran arama arayüzü açılıyor")
            IncomingCallActivity.launch(context, phoneNumber)
        }
    }
}
