package com.suanmesgulum.app.presentation.assistant

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.suanmesgulum.app.R
import com.suanmesgulum.app.domain.model.AssistantSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Asistan Özelleştirme Ekranı: ViewPager2 ile 3 sekme (Genel, Mesajlar, Sesler).
 */
@AndroidEntryPoint
class AssistantCustomizeActivity : AppCompatActivity() {

    private val viewModel: AssistantCustomizeViewModel by viewModels()

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var btnSave: MaterialButton

    private var generalFragment: AssistantGeneralFragment? = null
    private var messagesFragment: AssistantMessagesFragment? = null

    private val tabTitles by lazy {
        arrayOf(
            getString(R.string.assistant_tab_general),
            getString(R.string.assistant_tab_messages),
            getString(R.string.assistant_tab_voices)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assistant_customize)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        btnSave = findViewById(R.id.btnSaveSettings)

        setupViewPager()
        setupSaveButton()
        observeSave()
    }

    private fun setupViewPager() {
        generalFragment = AssistantGeneralFragment()
        messagesFragment = AssistantMessagesFragment()
        val voicesFragment = AssistantVoicesFragment()

        val adapter = AssistantPagerAdapter(this, listOf(
            generalFragment!!,
            messagesFragment!!,
            voicesFragment
        ))
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    private fun setupSaveButton() {
        btnSave.setOnClickListener {
            val currentSettings = viewModel.settings.value ?: return@setOnClickListener

            val updatedSettings = AssistantSettings(
                id = currentSettings.id,
                assistantName = generalFragment?.getAssistantName() ?: currentSettings.assistantName,
                personality = generalFragment?.getPersonality() ?: currentSettings.personality,
                defaultGreetingMessageId = currentSettings.defaultGreetingMessageId,
                defaultVoiceId = currentSettings.defaultVoiceId,
                defaultLanguage = generalFragment?.getLanguage() ?: currentSettings.defaultLanguage,
                spotifyConnected = currentSettings.spotifyConnected,
                spotifyPlaylistId = currentSettings.spotifyPlaylistId,
                infoGatheringMessage = messagesFragment?.getInfoGathering() ?: currentSettings.infoGatheringMessage,
                farewellMessage = messagesFragment?.getFarewell() ?: currentSettings.farewellMessage,
                voicemailPromptMessage = messagesFragment?.getVoicemailPrompt() ?: currentSettings.voicemailPromptMessage
            )

            viewModel.saveSettings(updatedSettings)
        }
    }

    private fun observeSave() {
        lifecycleScope.launch {
            viewModel.saveSuccess.collectLatest { success ->
                if (success) {
                    Toast.makeText(this@AssistantCustomizeActivity,
                        R.string.assistant_settings_saved, Toast.LENGTH_SHORT).show()
                    viewModel.resetSaveFlag()
                }
            }
        }
    }
}
