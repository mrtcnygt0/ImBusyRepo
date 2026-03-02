package com.suanmesgulum.app.presentation.voicemail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suanmesgulum.app.domain.model.Voicemail
import com.suanmesgulum.app.domain.repository.VoicemailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoicemailViewModel @Inject constructor(
    private val voicemailRepository: VoicemailRepository
) : ViewModel() {

    private val _voicemails = MutableStateFlow<List<Voicemail>>(emptyList())
    val voicemails: StateFlow<List<Voicemail>> = _voicemails.asStateFlow()

    init {
        loadVoicemails()
    }

    private fun loadVoicemails() {
        viewModelScope.launch {
            voicemailRepository.getAllVoicemails().collect { list ->
                _voicemails.value = list
            }
        }
    }

    fun markAsListened(id: Long) {
        viewModelScope.launch {
            voicemailRepository.markAsListened(id)
        }
    }

    fun archiveVoicemail(id: Long) {
        viewModelScope.launch {
            voicemailRepository.archiveVoicemail(id)
        }
    }

    fun deleteVoicemail(voicemail: Voicemail) {
        viewModelScope.launch {
            voicemailRepository.deleteVoicemail(voicemail.id)
        }
    }
}
