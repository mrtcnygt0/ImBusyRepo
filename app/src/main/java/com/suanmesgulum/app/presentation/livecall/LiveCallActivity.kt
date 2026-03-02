package com.suanmesgulum.app.presentation.livecall

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.suanmesgulum.app.R
import com.suanmesgulum.app.service.orchestrator.CallOrchestratorService
import com.suanmesgulum.app.service.orchestrator.CallOrchestratorService.OrchestratorState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Canlı Arama Ekranı: Asistan görüşmesini gerçek zamanlı izleme.
 *
 * CallOrchestratorService'in companion StateFlow'larını dinleyerek:
 * - Anlık transkript gösterimi
 * - Durum değişikliklerini izleme
 * - "Devam Et" / "Reddet" komutları gönderme
 */
class LiveCallActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_PHONE_NUMBER = "extra_phone_number"

        fun launch(context: Context, phoneNumber: String) {
            val intent = Intent(context, LiveCallActivity::class.java).apply {
                putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)
        }
    }

    private lateinit var tvCallerNumber: TextView
    private lateinit var tvCallState: TextView
    private lateinit var llTranscriptContainer: LinearLayout
    private lateinit var tvTranscriptEmpty: TextView
    private lateinit var scrollTranscript: NestedScrollView
    private lateinit var llPartialText: LinearLayout
    private lateinit var tvPartialText: TextView
    private lateinit var btnContinue: MaterialButton
    private lateinit var btnReject: MaterialButton
    private lateinit var llActionButtons: LinearLayout

    private var phoneNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Kilit ekranında göster
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        setContentView(R.layout.activity_live_call)

        phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER) ?: ""

        bindViews()
        setupButtons()
        observeOrchestratorState()
        observeTranscript()
        observeNewMessages()
    }

    private fun bindViews() {
        tvCallerNumber = findViewById(R.id.tvCallerNumber)
        tvCallState = findViewById(R.id.tvCallState)
        llTranscriptContainer = findViewById(R.id.llTranscriptContainer)
        tvTranscriptEmpty = findViewById(R.id.tvTranscriptEmpty)
        scrollTranscript = findViewById(R.id.scrollTranscript)
        llPartialText = findViewById(R.id.llPartialText)
        tvPartialText = findViewById(R.id.tvPartialText)
        btnContinue = findViewById(R.id.btnContinue)
        btnReject = findViewById(R.id.btnReject)
        llActionButtons = findViewById(R.id.llActionButtons)

        tvCallerNumber.text = getString(R.string.live_call_caller, phoneNumber)
    }

    private fun setupButtons() {
        btnContinue.setOnClickListener {
            // Asistan devam etsin komutu
            val intent = Intent(this, CallOrchestratorService::class.java).apply {
                action = CallOrchestratorService.ACTION_CONTINUE
            }
            startService(intent)
        }

        btnReject.setOnClickListener {
            // Reddet ve kapat komutu
            val intent = Intent(this, CallOrchestratorService::class.java).apply {
                action = CallOrchestratorService.ACTION_REJECT
            }
            startService(intent)
        }
    }

    private fun observeOrchestratorState() {
        lifecycleScope.launch {
            CallOrchestratorService.currentState.collectLatest { state ->
                updateStateUI(state)
            }
        }
    }

    private fun updateStateUI(state: OrchestratorState) {
        val statusText = when (state) {
            OrchestratorState.IDLE -> getString(R.string.live_call_status_idle)
            OrchestratorState.ANSWERING -> getString(R.string.live_call_status_answering)
            OrchestratorState.GREETING -> getString(R.string.live_call_status_greeting)
            OrchestratorState.LISTENING -> getString(R.string.live_call_status_listening)
            OrchestratorState.WAITING_USER -> getString(R.string.live_call_status_waiting)
            OrchestratorState.INFO_GATHERING -> getString(R.string.live_call_status_listening)
            OrchestratorState.FAREWELL -> getString(R.string.live_call_status_farewell)
            OrchestratorState.VOICEMAIL_PROMPT -> getString(R.string.live_call_status_voicemail)
            OrchestratorState.VOICEMAIL_RECORDING -> getString(R.string.live_call_status_voicemail)
            OrchestratorState.COMPLETED -> getString(R.string.live_call_status_completed)
        }

        val statusColor = when (state) {
            OrchestratorState.LISTENING, OrchestratorState.INFO_GATHERING -> 0xFF4CAF50.toInt()
            OrchestratorState.WAITING_USER -> 0xFFFF9800.toInt()
            OrchestratorState.COMPLETED -> 0xFF888888.toInt()
            OrchestratorState.FAREWELL -> 0xFFF44336.toInt()
            else -> 0xFF2196F3.toInt()
        }

        tvCallState.text = statusText
        tvCallState.setTextColor(statusColor)

        // WAITING_USER durumunda butonları göster
        val showButtons = state == OrchestratorState.WAITING_USER
        llActionButtons.visibility = if (showButtons) View.VISIBLE else View.GONE

        // COMPLETED durumunda 3 saniye sonra kapat
        if (state == OrchestratorState.COMPLETED) {
            btnContinue.isEnabled = false
            btnReject.isEnabled = false
            llActionButtons.visibility = View.GONE
            window.decorView.postDelayed({ finish() }, 3000)
        }
    }

    private fun observeTranscript() {
        lifecycleScope.launch {
            CallOrchestratorService.liveTranscript.collectLatest { transcript ->
                if (transcript.isNotBlank()) {
                    // Partial text göster
                    llPartialText.visibility = View.VISIBLE
                    tvPartialText.text = transcript
                } else {
                    llPartialText.visibility = View.GONE
                }
            }
        }
    }

    private fun observeNewMessages() {
        lifecycleScope.launch {
            CallOrchestratorService.newMessage.collect { message ->
                addMessageBubble(message.speaker, message.text)
            }
        }
    }

    /**
     * Transkript alanına yeni mesaj baloncuğu ekle.
     */
    private fun addMessageBubble(speaker: String, text: String) {
        if (tvTranscriptEmpty.visibility == View.VISIBLE) {
            tvTranscriptEmpty.visibility = View.GONE
        }

        val isAssistant = speaker == "ASISTAN"

        // Konuşmacı etiketi
        val labelView = TextView(this).apply {
            this.text = if (isAssistant) getString(R.string.live_call_assistant_label)
                        else getString(R.string.live_call_caller_label)
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
            setTextColor(if (isAssistant) 0xFF2196F3.toInt() else 0xFF4CAF50.toInt())
            gravity = if (isAssistant) Gravity.START else Gravity.END
            setPadding(8, 12, 8, 0)
        }

        // Mesaj metni
        val messageView = TextView(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = if (isAssistant) Gravity.START else Gravity.END
            setPadding(12, 4, 12, 8)
            setBackgroundColor(if (isAssistant) 0xFF1A237E.toInt() else 0xFF1B5E20.toInt())

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(
                if (isAssistant) 0 else 64,
                0,
                if (isAssistant) 64 else 0,
                4
            )
            layoutParams = params
        }

        llTranscriptContainer.addView(labelView)
        llTranscriptContainer.addView(messageView)

        // Otomatik scroll aşağı
        scrollTranscript.post {
            scrollTranscript.fullScroll(View.FOCUS_DOWN)
        }
    }

    override fun onBackPressed() {
        // Geri tuşunu engelleme — kullanıcı sadece butonlarla etkileşim kurabilir
        // Ama COMPLETED durumundaysa çıkmasına izin ver
        val state = CallOrchestratorService.currentState.value
        if (state == OrchestratorState.COMPLETED || state == OrchestratorState.IDLE) {
            super.onBackPressed()
        }
    }
}
