package com.suanmesgulum.app.presentation.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suanmesgulum.app.domain.model.AssistantSettings
import com.suanmesgulum.app.domain.repository.AssistantSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssistantCustomizeViewModel @Inject constructor(
    private val assistantSettingsRepository: AssistantSettingsRepository
) : ViewModel() {

    private val _settings = MutableStateFlow<AssistantSettings?>(null)
    val settings: StateFlow<AssistantSettings?> = _settings.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            assistantSettingsRepository.ensureDefaultSettings()
            assistantSettingsRepository.getSettings().collect { s ->
                _settings.value = s
            }
        }
    }

    fun saveSettings(settings: AssistantSettings) {
        viewModelScope.launch {
            assistantSettingsRepository.saveSettings(settings)
            _saveSuccess.value = true
        }
    }

    fun resetSaveFlag() {
        _saveSuccess.value = false
    }
}
