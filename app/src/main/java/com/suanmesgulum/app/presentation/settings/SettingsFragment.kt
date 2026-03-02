package com.suanmesgulum.app.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.suanmesgulum.app.R
import com.suanmesgulum.app.billing.BillingManager
import com.suanmesgulum.app.databinding.FragmentSettingsBinding
import com.suanmesgulum.app.service.ServicePreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Ayarlar Ekranı.
 *
 * - Dil seçimi (Türkçe/İngilizce)
 * - Gelişmiş TTS paketini satın alma
 * - Otomatik yanıtlama modu
 */
@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var prefs: ServicePreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = ServicePreferences(requireContext())

        setupUI()
        observeState()
    }

    /**
     * UI kurulumu.
     */
    private fun setupUI() {
        // Otomatik yanıtlama toggle
        binding.switchAutoAnswer.isChecked = prefs.isAutoAnswer
        binding.switchAutoAnswer.setOnCheckedChangeListener { _, isChecked ->
            prefs.isAutoAnswer = isChecked
        }

        // Dil seçimi
        val currentLang = prefs.language
        binding.rgLanguage.check(
            if (currentLang == "tr") R.id.rbTurkish else R.id.rbEnglish
        )
        binding.rgLanguage.setOnCheckedChangeListener { _, checkedId ->
            val lang = if (checkedId == R.id.rbTurkish) "tr" else "en"
            prefs.language = lang
            Toast.makeText(
                requireContext(),
                getString(R.string.language_changed),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Premium satın alma butonu
        binding.btnPurchasePremium.setOnClickListener {
            viewModel.purchasePremium(requireActivity())
        }

        // Test: Satın almayı simüle et
        binding.btnSimulatePurchase.setOnClickListener {
            viewModel.simulatePurchase()
        }
    }

    /**
     * State gözlemle.
     */
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Satın alma durumu
                launch {
                    viewModel.purchaseState.collect { state ->
                        updatePurchaseUI(state)
                    }
                }

                // Olaylar
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is SettingsViewModel.UiEvent.ShowMessage -> {
                                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Satın alma UI durumunu güncelle.
     */
    private fun updatePurchaseUI(state: BillingManager.PurchaseState) {
        when (state) {
            is BillingManager.PurchaseState.Purchased -> {
                binding.btnPurchasePremium.isEnabled = false
                binding.btnPurchasePremium.text = getString(R.string.premium_purchased)
                binding.tvPremiumStatus.text = getString(R.string.premium_active)
                binding.tvPremiumStatus.setTextColor(
                    requireContext().getColor(R.color.green_active)
                )
                binding.btnSimulatePurchase.visibility = View.GONE
                prefs.isPremiumPurchased = true
            }
            is BillingManager.PurchaseState.Pending -> {
                binding.btnPurchasePremium.isEnabled = false
                binding.btnPurchasePremium.text = getString(R.string.premium_pending)
                binding.tvPremiumStatus.text = getString(R.string.premium_pending)
            }
            is BillingManager.PurchaseState.Error -> {
                binding.btnPurchasePremium.isEnabled = true
                binding.tvPremiumStatus.text = state.message
                binding.tvPremiumStatus.setTextColor(
                    requireContext().getColor(R.color.red_inactive)
                )
            }
            is BillingManager.PurchaseState.NotPurchased -> {
                binding.btnPurchasePremium.isEnabled = true
                binding.btnPurchasePremium.text = getString(R.string.purchase_premium)
                binding.tvPremiumStatus.text = getString(R.string.premium_not_purchased)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
