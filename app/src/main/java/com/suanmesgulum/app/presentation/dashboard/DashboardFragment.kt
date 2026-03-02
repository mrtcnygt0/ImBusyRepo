package com.suanmesgulum.app.presentation.dashboard

import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.suanmesgulum.app.R
import com.suanmesgulum.app.databinding.FragmentDashboardBinding
import com.suanmesgulum.app.presentation.assistant.AssistantCustomizeActivity
import com.suanmesgulum.app.presentation.voicemail.VoicemailActivity
import com.suanmesgulum.app.service.BusyForegroundService
import com.suanmesgulum.app.service.ServicePreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Ana Ekran (Dashboard) Fragment'ı.
 *
 * Gösterir:
 * - CallScreeningService rol uyarısı (aktif değilse)
 * - Servis açma/kapama toggle
 * - Hızlı istatistikler (bugün reddedilen çağrılar, en çok kullanılan mod)
 * - "Modları Yönet" ve "Geçmiş Loglar" butonları
 */
@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var prefs: ServicePreferences

    /** CallScreeningService rol isteme launcher'ı */
    private val roleRequestLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            Toast.makeText(
                requireContext(),
                getString(R.string.call_screening_enabled),
                Toast.LENGTH_SHORT
            ).show()
        }
        checkCallScreeningRole()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = ServicePreferences(requireContext())

        setupUI()
        observeState()
        checkCallScreeningRole()
    }

    /**
     * UI bileşenlerini kur.
     */
    private fun setupUI() {
        // Servis toggle durumunu ayarla
        binding.switchService.isChecked = prefs.isServiceEnabled
        viewModel.setServiceActive(prefs.isServiceEnabled)

        // Servis toggle listener
        binding.switchService.setOnCheckedChangeListener { _, isChecked ->
            toggleService(isChecked)
        }

        // "Modları Yönet" butonu
        binding.btnManageModes.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_modes)
        }

        // "Geçmiş Loglar" butonu
        binding.btnViewLogs.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_logs)
        }

        // "Ayarlar" butonu
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_settings)
        }

        // "Rolü Etkinleştir" butonu
        binding.btnRequestRole.setOnClickListener {
            requestCallScreeningRole()
        }

        // v2: "Asistanı Özelleştir" butonu
        binding.btnCustomizeAssistant.setOnClickListener {
            startActivity(Intent(requireContext(), AssistantCustomizeActivity::class.java))
        }

        // v2: "Sesli Mesajlar" butonu
        binding.btnVoicemails.setOnClickListener {
            startActivity(Intent(requireContext(), VoicemailActivity::class.java))
        }
    }

    /**
     * CallScreeningService rolü kontrol et ve uyarı göster/gizle.
     */
    private fun checkCallScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = requireContext().getSystemService(RoleManager::class.java)
            if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                if (roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                    // Rol aktif, uyarıyı gizle
                    binding.cardRoleWarning.visibility = View.GONE
                } else {
                    // Rol aktif değil, uyarıyı göster
                    binding.cardRoleWarning.visibility = View.VISIBLE
                }
            } else {
                // Rol mevcut değil; yedek mekanizma bilgisi göster
                binding.cardRoleWarning.visibility = View.VISIBLE
                binding.btnRequestRole.visibility = View.GONE
            }
        } else {
            // Android 10 altı — CallScreeningService yok, uyarı gizle (PhoneStateReceiver aktif)
            binding.cardRoleWarning.visibility = View.GONE
        }
    }

    /**
     * CallScreeningService rolünü talep et.
     */
    private fun requestCallScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = requireContext().getSystemService(RoleManager::class.java)
            if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
            ) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                roleRequestLauncher.launch(intent)
            }
        }
    }

    /**
     * ViewModel durumunu gözlemle.
     */
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    /**
     * UI'ı güncelle.
     */
    private fun updateUI(state: DashboardViewModel.DashboardUiState) {
        binding.apply {
            // Servis durumu
            tvServiceStatus.text = if (state.isServiceActive) {
                getString(R.string.service_active)
            } else {
                getString(R.string.service_inactive)
            }

            tvServiceStatus.setTextColor(
                if (state.isServiceActive) {
                    requireContext().getColor(R.color.green_active)
                } else {
                    requireContext().getColor(R.color.red_inactive)
                }
            )

            // İstatistikler
            tvTodayCallCount.text = state.todayCallCount.toString()
            tvMostUsedMode.text = state.mostUsedMode
            tvTotalModes.text = state.totalModes.toString()
        }
    }

    /**
     * Foreground service'i aç/kapat.
     */
    private fun toggleService(enabled: Boolean) {
        viewModel.setServiceActive(enabled)
        prefs.isServiceEnabled = enabled

        val intent = Intent(requireContext(), BusyForegroundService::class.java)

        if (enabled) {
            intent.action = BusyForegroundService.ACTION_START
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().startForegroundService(intent)
            } else {
                requireContext().startService(intent)
            }
        } else {
            intent.action = BusyForegroundService.ACTION_STOP
            requireContext().startService(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
        binding.switchService.isChecked = prefs.isServiceEnabled
        checkCallScreeningRole()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
