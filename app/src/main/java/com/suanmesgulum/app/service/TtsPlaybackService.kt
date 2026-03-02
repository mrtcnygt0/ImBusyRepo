package com.suanmesgulum.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlin.coroutines.suspendCoroutine
import androidx.room.Room
import com.suanmesgulum.app.ImBusyApplication
import com.suanmesgulum.app.R
import com.suanmesgulum.app.data.local.AppDatabase
import com.suanmesgulum.app.data.local.entity.CallLogEntity
import com.suanmesgulum.app.tts.AndroidTtsEngine
import kotlinx.coroutines.*
import java.io.File

/**
 * TTS Oynatma Servisi.
 * Kullanıcı bir mod seçtiğinde aramayı cevaplar, TTS ile mesajı okur ve aramayı sonlandırır.
 *
 * Çalışma akışı:
 * 1. Aramayı cevapla (telecomManager.acceptRingingCall)
 * 2. Ses modunu arama moduna ayarla (MODE_IN_CALL)
 * 3. Metni TTS ile sentezle ve dosyaya kaydet
 * 4. Sentezlenen sesi MediaPlayer ile oynat
 * 5. Oynatma bitince aramayı sonlandır
 * 6. İşlemi logla
 */
class TtsPlaybackService : Service() {

    companion object {
        private const val TAG = "TtsPlaybackService"
        private const val NOTIFICATION_ID = 3001
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var ttsEngine: AndroidTtsEngine? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val phoneNumber = intent?.getStringExtra(BusyCallScreeningService.EXTRA_PHONE_NUMBER) ?: ""
        val modeId = intent?.getLongExtra(BusyCallScreeningService.EXTRA_MODE_ID, -1L) ?: -1L

        if (modeId == -1L) {
            Log.e(TAG, "Geçersiz mod ID'si")
            stopSelf()
            return START_NOT_STICKY
        }

        // Foreground service bildirimi göster
        startForeground(NOTIFICATION_ID, createNotification())

        // Ana işlem akışını başlat
        serviceScope.launch {
            try {
                processCall(phoneNumber, modeId)
            } catch (e: Exception) {
                Log.e(TAG, "Arama işleme hatası", e)
            } finally {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    /**
     * Aramayı işle: cevapla, TTS oku, kapat, logla.
     */
    private suspend fun processCall(phoneNumber: String, modeId: Long) {
        // 1. Mod bilgilerini al
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()

        val mode = db.customModeDao().getModeById(modeId)
        if (mode == null) {
            Log.e(TAG, "Mod bulunamadı: $modeId")
            db.close()
            return
        }

        Log.i(TAG, "Mod seçildi: ${mode.name} - ${mode.text}")

        // 2. Aramayı cevapla
        answerCall()

        // Kısa bir bekleme - cevaplama tamamlansın
        delay(1000)

        // 3. Ses modunu ayarla
        requestAudioFocus()
        setAudioMode(AudioManager.MODE_IN_COMMUNICATION)

        // 4. TTS ile metni sentezle ve dosyaya kaydet
        val ttsFile = File(cacheDir, "tts_output_${System.currentTimeMillis()}.wav")
        val ttsSuccess = synthesizeText(mode.text, ttsFile)

        if (ttsSuccess && ttsFile.exists()) {
            // 5. Ses dosyasını oynat
            playAudioFile(ttsFile)

            // Oynatma bitene kadar bekle
            waitForPlaybackCompletion()
        } else {
            Log.e(TAG, "TTS sentezleme başarısız oldu")
            // Yedek: Doğrudan TTS speak metodu ile dene
            speakDirectly(mode.text)
            delay(3000) // Konuşma için bekleme süresi
        }

        // 6. Aramayı sonlandır
        delay(500)
        endCall()

        // 7. Ses modunu eski haline getir
        setAudioMode(AudioManager.MODE_NORMAL)
        abandonAudioFocus()

        // 8. İşlemi logla
        val contactName = getContactName(phoneNumber)
        val prefs = ServicePreferences(this)
        val callLog = CallLogEntity(
            phoneNumber = phoneNumber,
            contactName = contactName,
            selectedModeName = mode.name,
            selectedModeText = mode.text,
            timestamp = System.currentTimeMillis(),
            isPaidFeatureUsed = prefs.isPremiumPurchased
        )
        db.callLogDao().insertLog(callLog)
        db.close()

        // Geçici dosyayı temizle
        ttsFile.delete()

        Log.i(TAG, "Arama işleme tamamlandı: $phoneNumber -> ${mode.name}")
    }

    /**
     * Gelen aramayı cevapla.
     */
    private fun answerCall() {
        try {
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                telecomManager.acceptRingingCall()
            }
            Log.i(TAG, "Arama cevaplandı")
        } catch (e: SecurityException) {
            Log.e(TAG, "Arama cevaplama izni yok", e)
        } catch (e: Exception) {
            Log.e(TAG, "Arama cevaplama hatası", e)
        }
    }

    /**
     * Aktif aramayı sonlandır.
     */
    private fun endCall() {
        try {
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                telecomManager.endCall()
            }
            Log.i(TAG, "Arama sonlandırıldı")
        } catch (e: SecurityException) {
            Log.e(TAG, "Arama sonlandırma izni yok", e)
        } catch (e: Exception) {
            Log.e(TAG, "Arama sonlandırma hatası", e)
        }
    }

    /**
     * Metni TTS ile sentezle ve dosyaya kaydet.
     */
    private suspend fun synthesizeText(text: String, outputFile: File): Boolean {
        val engine = AndroidTtsEngine(this)
        ttsEngine = engine

        val initialized = engine.initialize()
        if (!initialized) {
            Log.e(TAG, "TTS motoru başlatılamadı")
            return false
        }

        return engine.synthesizeToFile(text, outputFile)
    }

    /**
     * Metni doğrudan TTS speak ile oku (yedek yöntem).
     */
    private suspend fun speakDirectly(text: String) {
        val engine = ttsEngine ?: AndroidTtsEngine(this).also {
            it.initialize()
            ttsEngine = it
        }
        engine.speak(text)
    }

    /**
     * Ses dosyasını MediaPlayer ile oynat.
     */
    private fun playAudioFile(audioFile: File) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .build()
                )
                setDataSource(audioFile.absolutePath)
                prepare()
                start()
            }
            Log.i(TAG, "Ses oynatma başladı")
        } catch (e: Exception) {
            Log.e(TAG, "Ses oynatma hatası", e)
        }
    }

    /**
     * MediaPlayer oynatma tamamlanana kadar bekle.
     */
    private suspend fun waitForPlaybackCompletion() = suspendCoroutine { continuation ->
        mediaPlayer?.setOnCompletionListener {
            Log.i(TAG, "Ses oynatma tamamlandı")
            continuation.resumeWith(Result.success(Unit))
        }
        mediaPlayer?.setOnErrorListener { _, what, extra ->
            Log.e(TAG, "MediaPlayer hatası: what=$what, extra=$extra")
            continuation.resumeWith(Result.success(Unit))
            true
        }

        // MediaPlayer null ise hemen devam et
        if (mediaPlayer == null) {
            continuation.resumeWith(Result.success(Unit))
        }
    }

    /**
     * Ses odağı talep et.
     */
    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .build()
            audioManager?.requestAudioFocus(audioFocusRequest!!)
        }
    }

    /**
     * Ses odağını bırak.
     */
    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager?.abandonAudioFocusRequest(it)
            }
        }
    }

    /**
     * Ses modunu ayarla.
     */
    private fun setAudioMode(mode: Int) {
        audioManager?.mode = mode
    }

    /**
     * Rehberden kişi adını al.
     */
    private fun getContactName(phoneNumber: String): String? {
        if (phoneNumber.isBlank()) return null

        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )
            val cursor: Cursor? = contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null, null, null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
                } else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Kişi adı alma hatası", e)
            null
        }
    }

    /**
     * Foreground notification oluştur.
     */
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, ImBusyApplication.CHANNEL_SERVICE)
            .setContentTitle(getString(R.string.notification_tts_title))
            .setContentText(getString(R.string.notification_tts_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        ttsEngine?.shutdown()
        serviceScope.cancel()
        Log.i(TAG, "TtsPlaybackService yok edildi")
    }
}
