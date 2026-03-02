package com.suanmesgulum.app.presentation.assistant

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.suanmesgulum.app.R
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

/**
 * Asistan Ses sekmesi: TTS motorundaki mevcut sesleri listeler, önizleme sağlar.
 */
@AndroidEntryPoint
class AssistantVoicesFragment : Fragment(), TextToSpeech.OnInitListener {

    private lateinit var rvVoices: RecyclerView
    private var tts: TextToSpeech? = null
    private val voiceList = mutableListOf<VoiceItem>()
    private var selectedVoiceName: String? = null

    data class VoiceItem(
        val name: String,
        val locale: Locale,
        val isNetworkRequired: Boolean,
        var isSelected: Boolean = false
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_assistant_voices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvVoices = view.findViewById(R.id.rvVoices)
        rvVoices.layoutManager = LinearLayoutManager(requireContext())

        tts = TextToSpeech(requireContext(), this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            loadVoices()
        }
    }

    private fun loadVoices() {
        val voices = tts?.voices ?: return
        voiceList.clear()

        voices.filter { it.locale.language in listOf("tr", "en") }
            .sortedBy { it.name }
            .forEach { voice ->
                voiceList.add(VoiceItem(
                    name = voice.name,
                    locale = voice.locale,
                    isNetworkRequired = voice.isNetworkConnectionRequired
                ))
            }

        rvVoices.adapter = VoiceAdapter(voiceList) { item ->
            voiceList.forEach { it.isSelected = false }
            item.isSelected = true
            selectedVoiceName = item.name
            rvVoices.adapter?.notifyDataSetChanged()
            previewVoice(item)
        }
    }

    private fun previewVoice(item: VoiceItem) {
        tts?.let { engine ->
            val voice = engine.voices?.find { it.name == item.name }
            if (voice != null) {
                engine.voice = voice
                val sampleText = if (item.locale.language == "tr") {
                    "Merhaba, ben asistanınızım."
                } else {
                    "Hello, I am your assistant."
                }
                engine.speak(sampleText, TextToSpeech.QUEUE_FLUSH, null, "preview")
            }
        }
    }

    fun getSelectedVoiceName(): String? = selectedVoiceName

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    /**
     * İç RecyclerView.Adapter — ses listesi.
     */
    private class VoiceAdapter(
        private val items: List<VoiceItem>,
        private val onItemClick: (VoiceItem) -> Unit
    ) : RecyclerView.Adapter<VoiceAdapter.VH>() {

        inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvName: TextView = itemView.findViewById(android.R.id.text1)
            val tvDetails: TextView = itemView.findViewById(android.R.id.text2)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.tvName.text = item.name
            holder.tvDetails.text = "${item.locale.displayLanguage} ${if (item.isNetworkRequired) "(Online)" else "(Offline)"}"

            holder.itemView.setBackgroundColor(
                if (item.isSelected) 0x33448AFF.toInt() else 0x00000000
            )

            holder.itemView.setOnClickListener { onItemClick(item) }
        }

        override fun getItemCount(): Int = items.size
    }
}
