package com.suanmesgulum.app.presentation.assistant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.suanmesgulum.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Asistan Mesajlar sekmesi: Karşılama, bilgi toplama, veda, sesli mesaj yönlendirme.
 */
@AndroidEntryPoint
class AssistantMessagesFragment : Fragment() {

    private val viewModel: AssistantCustomizeViewModel by activityViewModels()

    private lateinit var etGreeting: TextInputEditText
    private lateinit var etInfoGathering: TextInputEditText
    private lateinit var etFarewell: TextInputEditText
    private lateinit var etVoicemailPrompt: TextInputEditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_assistant_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        etGreeting = view.findViewById(R.id.etGreeting)
        etInfoGathering = view.findViewById(R.id.etInfoGathering)
        etFarewell = view.findViewById(R.id.etFarewell)
        etVoicemailPrompt = view.findViewById(R.id.etVoicemailPrompt)

        observeSettings()
    }

    private fun observeSettings() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.settings.collectLatest { settings ->
                settings?.let {
                    if (etGreeting.text.isNullOrEmpty()) {
                        // greetingMessage alanı settings'te yok, ama default greeting
                        // entity'de defaultGreetingMessageId var — burada mesaj olarak kullanılır
                        etGreeting.setText(it.infoGatheringMessage.ifBlank {
                            getString(R.string.assistant_greeting_hint)
                        })
                    }
                    if (etInfoGathering.text.isNullOrEmpty()) etInfoGathering.setText(it.infoGatheringMessage)
                    if (etFarewell.text.isNullOrEmpty()) etFarewell.setText(it.farewellMessage)
                    if (etVoicemailPrompt.text.isNullOrEmpty()) etVoicemailPrompt.setText(it.voicemailPromptMessage)
                }
            }
        }
    }

    fun getGreeting(): String = etGreeting.text?.toString() ?: ""
    fun getInfoGathering(): String = etInfoGathering.text?.toString() ?: ""
    fun getFarewell(): String = etFarewell.text?.toString() ?: ""
    fun getVoicemailPrompt(): String = etVoicemailPrompt.text?.toString() ?: ""
}
