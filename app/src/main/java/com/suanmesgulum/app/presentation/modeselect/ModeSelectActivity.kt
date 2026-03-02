package com.suanmesgulum.app.presentation.modeselect

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.suanmesgulum.app.R
import com.suanmesgulum.app.databinding.ActivityModeSelectBinding
import com.suanmesgulum.app.domain.model.CustomMode
import com.suanmesgulum.app.service.BusyCallScreeningService
import com.suanmesgulum.app.service.TtsPlaybackService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Mod Seçim Activity'si.
 * Gelen arama bildiriminden açılır.
 * Kullanıcının hangi modu kullanacağını seçmesini sağlar.
 *
 * Dialog temalı küçük bir pencere olarak gösterilir.
 */
@AndroidEntryPoint
class ModeSelectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModeSelectBinding
    private val viewModel: ModeSelectViewModel by viewModels()
    private lateinit var adapter: ModeSelectAdapter
    private var phoneNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModeSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        phoneNumber = intent.getStringExtra(BusyCallScreeningService.EXTRA_PHONE_NUMBER) ?: ""

        setupUI()
        observeState()
    }

    private fun setupUI() {
        binding.tvCallerNumber.text = getString(R.string.caller_number, phoneNumber)

        adapter = ModeSelectAdapter { mode ->
            selectMode(mode)
        }

        binding.rvModeSelect.apply {
            adapter = this@ModeSelectActivity.adapter
            layoutManager = LinearLayoutManager(this@ModeSelectActivity)
        }

        // Sessizce reddet butonu
        binding.btnSilentReject.setOnClickListener {
            endCallSilently()
            finish()
        }

        // İptal butonu
        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.modes.collect { modes ->
                    adapter.submitList(modes)
                }
            }
        }
    }

    /**
     * Mod seçildiğinde TTS oynatma servisini başlat.
     */
    private fun selectMode(mode: CustomMode) {
        val serviceIntent = Intent(this, TtsPlaybackService::class.java).apply {
            putExtra(BusyCallScreeningService.EXTRA_PHONE_NUMBER, phoneNumber)
            putExtra(BusyCallScreeningService.EXTRA_MODE_ID, mode.id)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        Toast.makeText(
            this,
            getString(R.string.mode_selected, mode.name),
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }

    /**
     * Aramayı sessizce sonlandır.
     */
    private fun endCallSilently() {
        try {
            val telecomManager = getSystemService(TELECOM_SERVICE) as android.telecom.TelecomManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                telecomManager.endCall()
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, getString(R.string.error_end_call), Toast.LENGTH_SHORT).show()
        }
    }
}
