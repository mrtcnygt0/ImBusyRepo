package com.suanmesgulum.app.presentation.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suanmesgulum.app.domain.model.CallLogItem
import com.suanmesgulum.app.domain.repository.CallLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Geçmiş Loglar ekranı ViewModel'i.
 */
@HiltViewModel
class LogsViewModel @Inject constructor(
    private val callLogRepository: CallLogRepository
) : ViewModel() {

    /** Tüm logların listesi */
    val logs: StateFlow<List<CallLogItem>> = callLogRepository.getAllLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** UI olayları */
    sealed class UiEvent {
        data class ShowMessage(val message: String) : UiEvent()
    }

    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    /**
     * Tek bir logu sil.
     */
    fun deleteLog(logItem: CallLogItem) {
        viewModelScope.launch {
            callLogRepository.deleteLog(logItem.id)
            _events.emit(UiEvent.ShowMessage("Log kaydı silindi"))
        }
    }

    /**
     * Tüm logları temizle.
     */
    fun clearAllLogs() {
        viewModelScope.launch {
            callLogRepository.clearAllLogs()
            _events.emit(UiEvent.ShowMessage("Tüm loglar temizlendi"))
        }
    }
}
