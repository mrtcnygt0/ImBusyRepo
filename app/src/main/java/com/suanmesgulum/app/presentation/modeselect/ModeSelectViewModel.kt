package com.suanmesgulum.app.presentation.modeselect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suanmesgulum.app.domain.model.CustomMode
import com.suanmesgulum.app.domain.repository.CustomModeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Mod Seçim ekranı ViewModel'i.
 */
@HiltViewModel
class ModeSelectViewModel @Inject constructor(
    customModeRepository: CustomModeRepository
) : ViewModel() {

    val modes: StateFlow<List<CustomMode>> = customModeRepository.getAllModes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
