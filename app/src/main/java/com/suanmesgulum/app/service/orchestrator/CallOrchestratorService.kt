package com.suanmesgulum.app.service.orchestrator

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.suanmesgulum.app.ImBusyApplication
import com.suanmesgulum.app.R
import com.suanmesgulum.app.data.local.AppDatabase
import com.suanmesgulum.app.data.local.entity.CallLogEntity
import com.suanmesgulum.app.data.local.entity.CallMessageEntity
import com.suanmesgulum.app.data.local.entity.CallSessionEntity
import com.suanmesgulum.app.presentation.livecall.LiveCallActivity
import com.suanmesgulum.app.service.BusyCallScreeningService
import com.suanmesgulum.app.service.ServicePreferences
import com.suanmesgulum.app.service.stt.SpeechToTextManager
import com.suanmesgulum.app.tts.AndroidTtsEngine
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * CallOrchestrator — Çağrı Orkestra Şefi.
 *
 * Bir çağrı asistan tarafından cevaplandığında devreye giren ana yönetici.
 * TTS (asistanın konuşması), STT (arayanın dinlenmesi), transkript oluşturma
 * ve kullanıcı bildirimlerini koordine eder.
 *
 * Çalışma akışı:
 * 1. Çağrıyı cevapla
 * 2. Karşılama mesajını TTS ile oku
 * 3. Arayanın cevabını STT ile dinle ve metne çevir
 * 4. Metni LiveCallActivity'e ve bildirime göster
 * 5. Kullanıcı komutu bekle ("Devam Et" veya "Reddet")
 * 6. Devam → Bilgi alma mesajı oku, tekrar dinle (döngü)
 * 7. Reddet → Veda mesajını oku, çağrıyı kapat
 * 8. Döngü sonunda → Sesli mesaj bırakma teklif et
 * 9. Tüm konuşmayı DB'ye kaydet
 *
 * State Machine:
 * IDLE → GREETING → LISTENING → WAITING_USER → INFO_GATHERING → LISTENING → ...
 * ... → FAREWELL → VOICEMAIL_PROMPT → VOICEMAIL_RECORDING → COMPLETED
 */
class CallOrchestratorService : Service() {

    companion object {
        private const val TAG = "CallOrchestrator"
        private const val NOTIFICATION_ID = 5001
        private const val MAX_DIALOGUE_ROUNDS = 5

        // STT ayarları
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val LISTEN_TIMEOUT_MS = 10_000L // 10 saniye sessizlik timeout

        // Intent extras
        const val EXTRA_PHONE_NUMBER = "extra_phone_number"
        const val EXTRA_SESSION_ID = "extra_session_id"

        // User commands (LiveCallActivity'den gelen)
        const val ACTION_CONTINUE = "com.suanmesgulum.ACTION_ASSISTANT_CONTINUE"
        const val ACTION_REJECT = "com.suanmesgulum.ACTION_REJECT_CALL"

        // Canlı metin güncellemesi
        private val _liveTranscript = MutableStateFlow("")
        val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

        private val _currentState = MutableStateFlow(OrchestratorState.IDLE)
        val currentState: StateFlow<OrchestratorState> = _currentState.asStateFlow()

        private val _callerInfo = MutableStateFlow("")
        val callerInfo: StateFlow<String> = _callerInfo.asStateFlow()

        private val _sessionId = MutableStateFlow(0L)
        val sessionId: StateFlow<Long> = _sessionId.asStateFlow()

        private val _newMessage = MutableSharedFlow<TranscriptMessage>(replay = 0)
        val newMessage: SharedFlow<TranscriptMessage> = _newMessage.asSharedFlow()
    }

    /** Orkestra durumları */
    enum class OrchestratorState {
        IDLE,
        ANSWERING,
        GREETING,
        LISTENING,
        WAITING_USER,
        INFO_GATHERING,
        FAREWELL,
        VOICEMAIL_PROMPT,
        VOICEMAIL_RECORDING,
        COMPLETED
    }

    /** Transkript mesaj */
    data class TranscriptMessage(
        val speaker: String, // "ASISTAN" veya "ARAYAN"
        val text: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var ttsEngine: AndroidTtsEngine? = null
    private var sttManager: SpeechToTextManager? = null
    private var audioRecord: AudioRecord? = null
    private var audioManager: AudioManager? = null
    private var db: AppDatabase? = null

    private var phoneNumber: String = ""
    private var sessionDbId: Long = 0
    private var callLogId: Long = 0
    private var dialogueRound: Int = 0
    private var fullTranscript = StringBuilder()

    // Asistan ayarları — DB'den yüklenir
    private var assistantName = "Asistan"
    private var greetingMessage = ""
    private var infoGatheringMessage = ""
    private var farewellMessage = ""
    private var voicemailPromptMessage = ""
    private var userCommand: String? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONTINUE -> {
                userCommand = "CONTINUE"
                Log.i(TAG, "Kullanıcı komutu: Devam Et")
                return START_NOT_STICKY
            }
            ACTION_REJECT -> {
                userCommand = "REJECT"
                Log.i(TAG, "Kullanıcı komutu: Reddet")
                return START_NOT_STICKY
            }
        }

        phoneNumber = intent?.getStringExtra(EXTRA_PHONE_NUMBER) ?: ""
        _callerInfo.value = phoneNumber

        if (phoneNumber.isBlank()) {
            Log.e(TAG, "Telefon numarası boş, servis durduruluyor")
            stopSelf()
            return START_NOT_STICKY
        }

        // Foreground notification
        startForeground(NOTIFICATION_ID, createNotification("Asistan çağrıyı cevaplıyor..."))

        // Ana orkestra akışını başlat
        serviceScope.launch {
            try {
                runOrchestra()
            } catch (e: Exception) {
                Log.e(TAG, "Orkestra hatası", e)
            } finally {
                cleanup()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    /**
     * Ana orkestra akışı.
     */
    private suspend fun runOrchestra() {
        // 1. Veritabanı ve ayarları yükle
        initializeComponents()

        // 2. Çağrıyı cevapla
        _currentState.value = OrchestratorState.ANSWERING
        answerCall()
        delay(1000)

        // Ses modunu ayarla
        audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION

        // 3. CallLog ve CallSession oluştur
        createSessionInDb()

        // 4. LiveCallActivity'i aç
        launchLiveCallActivity()

        // 5. Karşılama mesajını oku
        _currentState.value = OrchestratorState.GREETING
        val greetText = greetingMessage.ifBlank {
            "Merhaba, $assistantName ben. Kullanıcımız şu anda müsait değil. Size nasıl yardımcı olabilirim?"
        }
        speakAndRecord(greetText, "ASISTAN")

        // 6. Diyalog döngüsü
        var continueDialogue = true
        while (continueDialogue && dialogueRound < MAX_DIALOGUE_ROUNDS) {
            dialogueRound++

            // Arayanı dinle
            _currentState.value = OrchestratorState.LISTENING
            val callerText = listenToCaller()

            if (callerText.isBlank()) {
                // Sessizlik — arayan bir şey söylemedi veya kapattı
                Log.i(TAG, "Arayanın sesi algılanamadı (sessizlik)")
                continueDialogue = false
                continue
            }

            // Kullanıcıya göster ve karar bekle
            _currentState.value = OrchestratorState.WAITING_USER
            updateNotification("Arayan: $callerText\n[Devam Et] veya [Reddet]")

            // Kullanıcı kararını bekle (max 15 saniye, yoksa devam et)
            userCommand = null
            val waitStart = System.currentTimeMillis()
            while (userCommand == null && System.currentTimeMillis() - waitStart < 15_000) {
                delay(500)
            }

            when (userCommand) {
                "REJECT" -> {
                    continueDialogue = false
                }
                "CONTINUE" -> {
                    // Bilgi alma mesajını oku
                    _currentState.value = OrchestratorState.INFO_GATHERING
                    val infoText = infoGatheringMessage.ifBlank {
                        "Anladım. Başka eklemek istediğiniz bir şey var mı?"
                    }
                    speakAndRecord(infoText, "ASISTAN")
                }
                null -> {
                    // Timeout — otomatik devam
                    _currentState.value = OrchestratorState.INFO_GATHERING
                    speakAndRecord(
                        infoGatheringMessage.ifBlank { "Anlıyorum. Devam edin lütfen." },
                        "ASISTAN"
                    )
                }
            }
        }

        // 7. Veda mesajı
        _currentState.value = OrchestratorState.FAREWELL
        val farewell = farewellMessage.ifBlank { "İlginiz için teşekkürler, iyi günler." }
        speakAndRecord(farewell, "ASISTAN")

        // 8. Sesli mesaj bırakma teklifi (opsiyonel)
        if (voicemailPromptMessage.isNotBlank()) {
            _currentState.value = OrchestratorState.VOICEMAIL_PROMPT
            speakAndRecord(voicemailPromptMessage, "ASISTAN")

            // Kısa bir süre dinle (sesli mesaj)
            _currentState.value = OrchestratorState.VOICEMAIL_RECORDING
            val voicemailText = listenToCaller(timeout = 15_000)
            if (voicemailText.isNotBlank()) {
                // Sesli mesajı kaydet
                saveVoicemail(voicemailText)
            }
        }

        // 9. Çağrıyı sonlandır
        _currentState.value = OrchestratorState.COMPLETED
        delay(500)
        endCall()

        // 10. Oturumu tamamla
        finalizeSession()
    }

    /**
     * Bileşenleri başlat — DB, TTS, STT.
     */
    private suspend fun initializeComponents() {
        // Veritabanı
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).addMigrations(AppDatabase.MIGRATION_1_2).build()

        // Asistan ayarlarını yükle
        val settings = db?.assistantSettingsDao()?.getSettingsSync()
        if (settings != null) {
            assistantName = settings.assistantName
            infoGatheringMessage = settings.infoGatheringMessage
            farewellMessage = settings.farewellMessage
            voicemailPromptMessage = settings.voicemailPromptMessage

            // Varsayılan karşılama mesajını yükle
            if (settings.defaultGreetingMessageId > 0) {
                val mode = db?.customModeDao()?.getModeById(settings.defaultGreetingMessageId)
                greetingMessage = mode?.text ?: ""
            }
        }

        // TTS motoru
        ttsEngine = AndroidTtsEngine(this).also {
            it.initialize()
        }

        // STT motoru (Vosk)
        sttManager = SpeechToTextManager(this).also {
            it.initialize()
        }
    }

    /**
     * DB'de CallLog ve CallSession oluştur.
     */
    private suspend fun createSessionInDb() {
        val contactName = getContactName(phoneNumber)

        val callLog = CallLogEntity(
            phoneNumber = phoneNumber,
            contactName = contactName,
            selectedModeName = "Asistan",
            selectedModeText = greetingMessage,
            timestamp = System.currentTimeMillis()
        )
        callLogId = db?.callLogDao()?.insertLog(callLog) ?: 0

        val session = CallSessionEntity(
            callLogId = callLogId,
            startTime = System.currentTimeMillis(),
            status = "DEVAM_EDIYOR"
        )
        sessionDbId = db?.callSessionDao()?.insertSession(session) ?: 0
        _sessionId.value = sessionDbId

        Log.i(TAG, "Oturum oluşturuldu: sessionId=$sessionDbId, callLogId=$callLogId")
    }

    /**
     * TTS ile konuş ve mesajı kaydet.
     */
    private suspend fun speakAndRecord(text: String, speaker: String) {
        // Metni transcript'e ekle
        fullTranscript.append("[$speaker] $text\n")
        _liveTranscript.value = fullTranscript.toString()

        // DB'ye mesajı kaydet
        val message = CallMessageEntity(
            sessionId = sessionDbId,
            speaker = speaker,
            messageText = text
        )
        db?.callMessageDao()?.insertMessage(message)

        // Flow'a yeni mesaj yolla
        serviceScope.launch {
            _newMessage.emit(TranscriptMessage(speaker, text))
        }

        // TTS ile seslendir
        val ttsFile = File(cacheDir, "tts_${System.currentTimeMillis()}.wav")
        val success = ttsEngine?.synthesizeToFile(text, ttsFile) ?: false

        if (success && ttsFile.exists()) {
            // Ses dosyasını oynat
            val player = android.media.MediaPlayer().apply {
                setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(android.media.AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .build()
                )
                setDataSource(ttsFile.absolutePath)
                prepare()
                start()
            }
            // Oynatma tamamlanana kadar bekle
            suspendCoroutine<Unit> { cont ->
                player.setOnCompletionListener {
                    player.release()
                    cont.resumeWith(Result.success(Unit))
                }
                player.setOnErrorListener { _, _, _ ->
                    player.release()
                    cont.resumeWith(Result.success(Unit))
                    true
                }
            }
            ttsFile.delete()
        } else {
            // Yedek: doğrudan TTS speak
            ttsEngine?.speak(text)
            delay(2000)
        }
    }

    /**
     * Arayanı dinle ve STT ile metne çevir.
     * @param timeout Dinleme süresi (ms)
     * @return Tanınan metin
     */
    @Suppress("MissingPermission")
    private suspend fun listenToCaller(timeout: Long = LISTEN_TIMEOUT_MS): String {
        val stt = sttManager ?: return ""

        // Vosk modeli yüklenmemişse boş döndür
        if (!stt.isModelLoaded.value) {
            Log.w(TAG, "Vosk modeli yüklenmemiş, dinleme atlanıyor")
            // Basit bekleme — arayan konuşurken sadece zaman tut
            delay(timeout)
            return "[STT model yüklenmemiş]"
        }

        try {
            val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize * 2
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord başlatılamadı")
                return ""
            }

            audioRecord?.startRecording()
            stt.startListening()

            val buffer = ByteArray(bufferSize)
            val startTime = System.currentTimeMillis()
            var collectedText = StringBuilder()

            // STT sonuçlarını topla
            val collectJob = serviceScope.launch {
                stt.finalResult.collect { text ->
                    if (text.isNotBlank()) {
                        collectedText.append(text).append(" ")
                        // Arayan metni transcript'e ekle and yayınla
                        fullTranscript.append("[ARAYAN] $text\n")
                        _liveTranscript.value = fullTranscript.toString()

                        db?.callMessageDao()?.insertMessage(
                            CallMessageEntity(
                                sessionId = sessionDbId,
                                speaker = "ARAYAN",
                                messageText = text
                            )
                        )

                        _newMessage.emit(TranscriptMessage("ARAYAN", text))
                    }
                }
            }

            val partialJob = serviceScope.launch {
                stt.partialResult.collect { partial ->
                    // Gerçek zamanlı güncelleme
                    if (partial.isNotBlank()) {
                        _liveTranscript.value = fullTranscript.toString() + "[ARAYAN (canlı)] $partial"
                    }
                }
            }

            // Ses verisi besle
            while (System.currentTimeMillis() - startTime < timeout) {
                val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: break
                if (bytesRead > 0) {
                    stt.feedAudioData(buffer, bytesRead)
                }
                delay(50) // CPU'yu rahatlatmak için kısa bekleme
            }

            // Dinlemeyi durdur
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null

            val lastText = stt.stopListening()
            if (lastText.isNotBlank()) {
                collectedText.append(lastText)
            }

            collectJob.cancel()
            partialJob.cancel()

            val result = collectedText.toString().trim()
            Log.i(TAG, "Arayanın söylediği: $result")
            return result

        } catch (e: Exception) {
            Log.e(TAG, "Dinleme hatası", e)
            audioRecord?.release()
            audioRecord = null
            return ""
        }
    }

    /**
     * Sesli mesajı kaydet.
     */
    private suspend fun saveVoicemail(text: String) {
        try {
            val contactName = getContactName(phoneNumber)
            // Sesli mesaj dosyası (burada sadece transkript kaydediyoruz)
            val voicemailDir = File(filesDir, "voicemails")
            if (!voicemailDir.exists()) voicemailDir.mkdirs()

            val audioFile = File(voicemailDir, "vm_${System.currentTimeMillis()}.txt")
            audioFile.writeText(text)

            db?.voicemailDao()?.insertVoicemail(
                com.suanmesgulum.app.data.local.entity.VoicemailEntity(
                    callerNumber = phoneNumber,
                    callerName = contactName,
                    audioFilePath = audioFile.absolutePath,
                    transcript = text,
                    duration = 0
                )
            )

            Log.i(TAG, "Sesli mesaj kaydedildi: $text")
        } catch (e: Exception) {
            Log.e(TAG, "Sesli mesaj kaydetme hatası", e)
        }
    }

    /**
     * Oturumu tamamla — transcript ve durumu güncelle.
     */
    private suspend fun finalizeSession() {
        try {
            db?.callSessionDao()?.updateSessionStatus(
                sessionDbId,
                "TAMAMLANDI",
                System.currentTimeMillis()
            )
            db?.callSessionDao()?.updateTranscript(
                sessionDbId,
                fullTranscript.toString()
            )
            Log.i(TAG, "Oturum tamamlandı: $sessionDbId")
        } catch (e: Exception) {
            Log.e(TAG, "Oturum tamamlama hatası", e)
        }
    }

    /**
     * LiveCallActivity'i aç.
     */
    private fun launchLiveCallActivity() {
        val intent = Intent(this, LiveCallActivity::class.java).apply {
            putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
            putExtra(EXTRA_SESSION_ID, sessionDbId)
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
        }
        startActivity(intent)
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
     * Rehberden kişi adını al.
     */
    private fun getContactName(phone: String): String? {
        if (phone.isBlank()) return null
        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phone)
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
            null
        }
    }

    /**
     * Foreground notification.
     */
    private fun createNotification(text: String): Notification {
        val intent = Intent(this, LiveCallActivity::class.java).apply {
            putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
            putExtra(EXTRA_SESSION_ID, sessionDbId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, ImBusyApplication.CHANNEL_INCOMING_CALL)
            .setContentTitle(getString(R.string.notification_orchestrator_title))
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    /**
     * Bildirimi güncelle.
     */
    private fun updateNotification(text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, createNotification(text))
    }

    /**
     * Kaynakları temizle.
     */
    private fun cleanup() {
        _currentState.value = OrchestratorState.IDLE
        _liveTranscript.value = ""
        _callerInfo.value = ""
        _sessionId.value = 0

        audioRecord?.release()
        audioRecord = null
        ttsEngine?.shutdown()
        sttManager?.shutdown()
        audioManager?.mode = AudioManager.MODE_NORMAL
        db?.close()
        serviceScope.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanup()
        Log.i(TAG, "CallOrchestrator yok edildi")
    }

    /**
     * suspendCoroutine yardımcısı
     */
    private suspend fun <T> suspendCoroutine(block: (kotlin.coroutines.Continuation<T>) -> Unit): T {
        return kotlin.coroutines.suspendCoroutine(block)
    }
}
