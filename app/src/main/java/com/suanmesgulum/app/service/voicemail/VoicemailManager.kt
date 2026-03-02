package com.suanmesgulum.app.service.voicemail

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.suanmesgulum.app.ImBusyApplication
import com.suanmesgulum.app.R
import com.suanmesgulum.app.domain.model.Voicemail
import com.suanmesgulum.app.domain.repository.VoicemailRepository
import com.suanmesgulum.app.presentation.MainActivity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sesli mesaj yöneticisi.
 *
 * Yeni sesli mesaj geldiğinde bildirim gönderir.
 * Mesajların listelenmesi, silinmesi, paylaşılması işlemlerini
 * repository aracılığıyla yapar.
 */
@Singleton
class VoicemailManager @Inject constructor(
    private val context: Context,
    private val voicemailRepository: VoicemailRepository
) {
    companion object {
        private const val TAG = "VoicemailManager"
        private const val NOTIFICATION_ID_VOICEMAIL = 4001
    }

    /**
     * Yeni sesli mesajı kaydet ve bildirim gönder.
     */
    suspend fun saveVoicemail(
        callerNumber: String,
        callerName: String?,
        audioFilePath: String,
        transcript: String?,
        duration: Int
    ): Long {
        val voicemail = Voicemail(
            callerNumber = callerNumber,
            callerName = callerName,
            audioFilePath = audioFilePath,
            transcript = transcript,
            duration = duration
        )

        val id = voicemailRepository.insertVoicemail(voicemail)
        Log.i(TAG, "Sesli mesaj kaydedildi: id=$id, arayan=$callerNumber")

        // Bildirim gönder
        sendVoicemailNotification(callerName ?: callerNumber, transcript)

        return id
    }

    /**
     * Tüm sesli mesajların listesi.
     */
    fun getAllVoicemails(): Flow<List<Voicemail>> {
        return voicemailRepository.getAllVoicemails()
    }

    /**
     * Dinlenmemiş mesaj sayısı.
     */
    fun getUnlistenedCount(): Flow<Int> {
        return voicemailRepository.getUnlistenedCount()
    }

    /**
     * Mesajı dinlenmiş olarak işaretle.
     */
    suspend fun markAsListened(id: Long) {
        voicemailRepository.markAsListened(id)
    }

    /**
     * Mesajı arşivle.
     */
    suspend fun archiveVoicemail(id: Long) {
        voicemailRepository.archiveVoicemail(id)
    }

    /**
     * Mesajı sil.
     */
    suspend fun deleteVoicemail(id: Long) {
        voicemailRepository.deleteVoicemail(id)
    }

    /**
     * Yeni sesli mesaj bildirimi gönder.
     */
    private fun sendVoicemailNotification(caller: String, transcript: String?) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "voicemail")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, NOTIFICATION_ID_VOICEMAIL, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val transcriptPreview = transcript?.take(100) ?: context.getString(R.string.voicemail_no_transcript)

        val notification = NotificationCompat.Builder(context, ImBusyApplication.CHANNEL_GENERAL)
            .setSmallIcon(R.drawable.ic_voicemail)
            .setContentTitle(context.getString(R.string.notification_voicemail_title))
            .setContentText(context.getString(R.string.voicemail_from, caller))
            .setStyle(NotificationCompat.BigTextStyle().bigText(transcriptPreview))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_VOICEMAIL, notification)
    }
}
