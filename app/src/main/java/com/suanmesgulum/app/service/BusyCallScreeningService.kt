package com.suanmesgulum.app.service

import android.content.Intent
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.suanmesgulum.app.presentation.incomingcall.IncomingCallActivity
import kotlinx.coroutines.*

/**
 * CallScreeningService: Gelen aramaları yakalama ve müdahale etme.
 *
 * Android'in CallScreeningService'i, gelen aramaları yakalamak ve
 * kullanıcı kararıyla işlemek için kullanılır.
 *
 * Çalışma mantığı:
 * 1. Arama geldiğinde onScreenCall çağrılır
 * 2. Servis aktifse → IncomingCallActivity tam ekran açılır
 * 3. Kullanıcı arama ekranından mod seçer veya reddeder
 * 4. Seçim yapılınca TtsPlaybackService devreye girer
 */
class BusyCallScreeningService : CallScreeningService() {

    companion object {
        private const val TAG = "BusyCallScreening"
        const val NOTIFICATION_ID_INCOMING = 2001
        const val EXTRA_PHONE_NUMBER = "extra_phone_number"
        const val EXTRA_MODE_ID = "extra_mode_id"
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
        if (prefs.isAutoAnswer && prefs.defaultModeId != -1L) {
            Log.i(TAG, "Otomatik yanıtlama modu aktif")
            handleAutoAnswer(callDetails, phoneNumber)
            return
        }

        // Manuel mod: Tam ekran arama arayüzünü aç
        Log.i(TAG, "Tam ekran arama arayüzü açılıyor")
        IncomingCallActivity.launch(this, phoneNumber)

        // Aramayı normal şekilde çalmaya devam ettir
        val response = CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .setSilenceCall(false)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()

        respondToCall(callDetails, response)
    }

    /**
     * Otomatik yanıtlama: Varsayılan modu kullanarak aramayı işle.
     */
    private fun handleAutoAnswer(callDetails: Call.Details, phoneNumber: String) {
        val response = CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .setSilenceCall(true)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()

        respondToCall(callDetails, response)

        // TTS oynatma servisini başlat
        val intent = Intent(this, TtsPlaybackService::class.java).apply {
            putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
            putExtra(EXTRA_MODE_ID, prefs.defaultModeId)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
