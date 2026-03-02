package com.suanmesgulum.app.presentation.modes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suanmesgulum.app.domain.model.CustomMode
import com.suanmesgulum.app.domain.repository.CustomModeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Mod Yönetim ekranı ViewModel'i.
 * Modların CRUD işlemlerini yönetir.
 */
@HiltViewModel
class ModesViewModel @Inject constructor(
    private val customModeRepository: CustomModeRepository
) : ViewModel() {

    /** Tüm modların listesi */
    val modes: StateFlow<List<CustomMode>> = customModeRepository.getAllModes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** UI olay akışı */
    sealed class UiEvent {
        data class ShowMessage(val message: String) : UiEvent()
        data class EditMode(val mode: CustomMode) : UiEvent()
    }

    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    /**
     * Yeni mod ekle.
     */
    fun addMode(name: String, text: String) {
        viewModelScope.launch {
            val modeCount = customModeRepository.getModeCount()
            val mode = CustomMode(
                name = name.trim(),
                text = text.trim(),
                orderIndex = modeCount,
                isDefault = modeCount == 0 // İlk mod varsayılan olsun
            )
            customModeRepository.insertMode(mode)
            _events.emit(UiEvent.ShowMessage("\"$name\" modu eklendi"))
        }
    }

    /**
     * Modu güncelle.
     */
    fun updateMode(id: Long, name: String, text: String) {
        viewModelScope.launch {
            val existing = customModeRepository.getModeById(id) ?: return@launch
            val updated = existing.copy(
                name = name.trim(),
                text = text.trim()
            )
            customModeRepository.updateMode(updated)
            _events.emit(UiEvent.ShowMessage("\"$name\" modu güncellendi"))
        }
    }

    /**
     * Modu sil.
     */
    fun deleteMode(mode: CustomMode) {
        viewModelScope.launch {
            if (mode.isDefault) {
                _events.emit(UiEvent.ShowMessage("Varsayılan mod silinemez"))
                return@launch
            }
            customModeRepository.deleteMode(mode.id)
            _events.emit(UiEvent.ShowMessage("\"${mode.name}\" modu silindi"))
        }
    }

    /**
     * Mod düzenleme isteği.
     */
    fun requestEdit(mode: CustomMode) {
        viewModelScope.launch {
            _events.emit(UiEvent.EditMode(mode))
        }
    }

    /**
     * Varsayılan modları oluştur (ilk çalıştırmada).
     */
    fun createDefaultModes() {
        viewModelScope.launch {
            if (customModeRepository.getModeCount() > 0) return@launch

            val defaultModes = listOf(
                CustomMode(
                    name = "Genel Meşgul",
                    text = "Şu anda meşgulüm, daha sonra arayayım mı?",
                    orderIndex = 0,
                    isDefault = true
                ),
                CustomMode(
                    name = "Toplantı",
                    text = "Toplantıdayım, ben seni sonra arayacağım.",
                    orderIndex = 1,
                    isDefault = false
                ),
                CustomMode(
                    name = "Teşekkür",
                    text = "Aradığınız için teşekkürler fakat şu anda meşgulüm. En kısa sürede sizi arayacağım.",
                    orderIndex = 2,
                    isDefault = false
                )
            )

            defaultModes.forEach { mode ->
                customModeRepository.insertMode(mode)
            }

            _events.emit(UiEvent.ShowMessage("Varsayılan modlar oluşturuldu"))
        }
    }
}
