package com.suanmesgulum.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suanmesgulum.app.domain.repository.CallLogRepository
import com.suanmesgulum.app.domain.repository.CustomModeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Dashboard ekranı ViewModel'i.
 * Servis durumu, istatistikler ve hızlı aksiyonları yönetir.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val callLogRepository: CallLogRepository,
    private val customModeRepository: CustomModeRepository
) : ViewModel() {

    /** Dashboard UI durumu */
    data class DashboardUiState(
        val isServiceActive: Boolean = false,
        val todayCallCount: Int = 0,
        val mostUsedMode: String = "-",
        val totalModes: Int = 0
    )

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    /**
     * Dashboard verilerini yükle.
     */
    private fun loadDashboardData() {
        viewModelScope.launch {
            val todayCount = callLogRepository.getTodayCallCount()
            val mostUsed = callLogRepository.getMostUsedModeName() ?: "-"
            val modeCount = customModeRepository.getModeCount()

            _uiState.update {
                it.copy(
                    todayCallCount = todayCount,
                    mostUsedMode = mostUsed,
                    totalModes = modeCount
                )
            }
        }
    }

    /**
     * Servis durumunu güncelle.
     */
    fun setServiceActive(active: Boolean) {
        _uiState.update { it.copy(isServiceActive = active) }
    }

    /**
     * Verileri yenile.
     */
    fun refresh() {
        loadDashboardData()
    }
}
