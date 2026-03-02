package com.suanmesgulum.app.presentation.assistant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.suanmesgulum.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Asistan Genel Ayarlar sekmesi: İsim, kişilik, dil.
 */
@AndroidEntryPoint
class AssistantGeneralFragment : Fragment() {

    private val viewModel: AssistantCustomizeViewModel by activityViewModels()

    private lateinit var etName: TextInputEditText
    private lateinit var etPersonality: TextInputEditText
    private lateinit var chipTurkish: Chip
    private lateinit var chipEnglish: Chip

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_assistant_general, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        etName = view.findViewById(R.id.etAssistantName)
        etPersonality = view.findViewById(R.id.etPersonality)
        chipTurkish = view.findViewById(R.id.chipTurkish)
        chipEnglish = view.findViewById(R.id.chipEnglish)

        observeSettings()
    }

    private fun observeSettings() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.settings.collectLatest { settings ->
                settings?.let {
                    if (etName.text.isNullOrEmpty()) etName.setText(it.assistantName)
                    if (etPersonality.text.isNullOrEmpty()) etPersonality.setText(it.personality)
                    when (it.defaultLanguage) {
                        "tr" -> chipTurkish.isChecked = true
                        "en" -> chipEnglish.isChecked = true
                    }
                }
            }
        }
    }

    fun getAssistantName(): String = etName.text?.toString() ?: ""
    fun getPersonality(): String = etPersonality.text?.toString() ?: ""
    fun getLanguage(): String = when {
        chipEnglish.isChecked -> "en"
        else -> "tr"
    }
}
