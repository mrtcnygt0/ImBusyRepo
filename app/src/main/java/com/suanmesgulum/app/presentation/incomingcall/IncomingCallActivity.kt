package com.suanmesgulum.app.presentation.incomingcall

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.util.Log
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.room.Room
import com.google.android.material.button.MaterialButton
import com.suanmesgulum.app.R
import com.suanmesgulum.app.data.local.AppDatabase
import com.suanmesgulum.app.data.local.entity.CustomModeEntity
import com.suanmesgulum.app.databinding.ActivityIncomingCallBinding
import com.suanmesgulum.app.presentation.modeselect.ModeSelectActivity
import com.suanmesgulum.app.service.BusyCallScreeningService
import com.suanmesgulum.app.service.BusyForegroundService
import com.suanmesgulum.app.service.TtsPlaybackService
import kotlinx.coroutines.*

/**
 * Tam Ekran Gelen Arama Aktivitesi.
 *
 * Arama geldiğinde ekranın üstünde (kilit ekranı dahil) görünen,
 * uygulamanın kendi arama arayüzü. Kullanıcı buradan:
 * - Bir meşguliyet modu seçerek TTS ile yanıtlayabilir
 * - Aramayı sessizce reddedebilir
 * - Aramayı görmezden gelip normal çalmasına bırakabilir
 */
class IncomingCallActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "IncomingCallActivity"
        const val EXTRA_PHONE_NUMBER = "extra_phone_number"

        /**
         * IncomingCallActivity'yi başlat.
         */
        fun launch(context: Context, phoneNumber: String) {
            val intent = Intent(context, IncomingCallActivity::class.java).apply {
                putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION
                )
            }
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityIncomingCallBinding
    private val activityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var phoneNumber: String = ""
    private var modes: List<CustomModeEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Kilit ekranı üzerinde, ekranı açarak göster
        configureWindowForLockScreen()

        binding = ActivityIncomingCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER) ?: ""

        setupCallerInfo()
        setupActions()
        loadModes()
    }

    /**
     * Kilit ekranı ve tam ekran ayarları.
     */
    private fun configureWindowForLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        // Tam ekran ve ekranı açık tut
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )
    }

    /**
     * Arayan bilgilerini ayarla.
     */
    private fun setupCallerInfo() {
        if (phoneNumber.isNotBlank()) {
            binding.tvCallerNumber.text = phoneNumber

            // Rehberden isim çek
            activityScope.launch(Dispatchers.IO) {
                val contactName = getContactName(phoneNumber)
                if (contactName != null) {
                    withContext(Dispatchers.Main) {
                        binding.tvCallerName.text = contactName
                        binding.tvCallerName.isVisible = true
                    }
                }
            }
        } else {
            binding.tvCallerNumber.text = getString(R.string.unknown_caller)
        }
    }

    /**
     * Aksiyon butonlarını ayarla.
     */
    private fun setupActions() {
        // Sessizce Reddet
        binding.btnSilentReject.setOnClickListener {
            rejectCall()
        }

        // Aramayı Geçir (müdahale etmeden kapat)
        binding.btnIgnore.setOnClickListener {
            finish()
        }

        // Tüm Modları Göster
        binding.btnShowAllModes.setOnClickListener {
            val intent = Intent(this, ModeSelectActivity::class.java).apply {
                putExtra(BusyCallScreeningService.EXTRA_PHONE_NUMBER, phoneNumber)
            }
            startActivity(intent)
            finish()
        }
    }

    /**
     * Modları veritabanından yükle ve buton olarak ekle.
     */
    private fun loadModes() {
        activityScope.launch(Dispatchers.IO) {
            try {
                val db = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    AppDatabase.DATABASE_NAME
                ).build()

                modes = db.customModeDao().getTopModes()
                db.close()

                withContext(Dispatchers.Main) {
                    populateModeButtons()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Mod yükleme hatası", e)
            }
        }
    }

    /**
     * Mod butonlarını dinamik olarak oluştur.
     */
    private fun populateModeButtons() {
        val container = binding.layoutModeButtons
        container.removeAllViews()

        if (modes.isEmpty()) {
            binding.btnShowAllModes.text = getString(R.string.no_modes_short)
            return
        }

        for (mode in modes) {
            val button = MaterialButton(
                this,
                null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle
            ).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    resources.getDimensionPixelSize(R.dimen.button_height)
                ).apply {
                    bottomMargin = 12.dpToPx()
                }
                text = mode.name
                setTextColor(0xFFFFFFFF.toInt())
                textSize = 16f
                cornerRadius = 28.dpToPx()
                strokeColor = android.content.res.ColorStateList.valueOf(0x40FFFFFF)
                strokeWidth = 1.dpToPx()
                rippleColor = android.content.res.ColorStateList.valueOf(0x30FFFFFF)
                setBackgroundColor(0x20FFFFFF)

                setOnClickListener {
                    selectMode(mode)
                }
            }
            container.addView(button)
        }

        // 3'ten fazla mod varsa "Tüm Modları Göster" butonunu göster
        binding.btnShowAllModes.isVisible = modes.size >= 3
    }

    /**
     * Bir mod seçildiğinde TtsPlaybackService başlat.
     */
    private fun selectMode(mode: CustomModeEntity) {
        Log.i(TAG, "Mod seçildi: ${mode.name}")

        binding.tvStatus.text = getString(R.string.mode_activating, mode.name)

        val serviceIntent = Intent(this, TtsPlaybackService::class.java).apply {
            putExtra(BusyCallScreeningService.EXTRA_PHONE_NUMBER, phoneNumber)
            putExtra(BusyCallScreeningService.EXTRA_MODE_ID, mode.id)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        // Kısa bir süre sonra ekranı kapat
        activityScope.launch {
            delay(800)
            finish()
        }
    }

    /**
     * Aramayı sessizce reddet.
     */
    private fun rejectCall() {
        Log.i(TAG, "Arama sessizce reddediliyor: $phoneNumber")

        try {
            val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                telecomManager.endCall()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Arama sonlandırma izni yok", e)
            Toast.makeText(this, getString(R.string.error_end_call), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Arama sonlandırma hatası", e)
        }

        finish()
    }

    /**
     * Rehberden kişi adını al.
     */
    private fun getContactName(phone: String): String? {
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
            Log.e(TAG, "Kişi adı sorgulama hatası", e)
            null
        }
    }

    /**
     * dp → px dönüştürücü.
     */
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel()
    }
}
