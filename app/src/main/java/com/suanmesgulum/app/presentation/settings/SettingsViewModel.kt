package com.suanmesgulum.app.presentation.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suanmesgulum.app.billing.BillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Ayarlar ekranı ViewModel'i.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val billingManager: BillingManager
) : ViewModel() {

    /** Premium paket satın alma durumu */
    val purchaseState = billingManager.purchaseState

    /** Ürün detayları */
    val productDetails = billingManager.productDetails

    /** UI olayları */
    sealed class UiEvent {
        data class ShowMessage(val message: String) : UiEvent()
    }

    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    init {
        billingManager.initialize()
    }

    /**
     * Premium paket satın alma akışını başlat.
     */
    fun purchasePremium(activity: Activity) {
        billingManager.launchPurchaseFlow(activity)
    }

    /**
     * Test: Satın almayı simüle et.
     */
    fun simulatePurchase() {
        billingManager.simulatePurchase()
        viewModelScope.launch {
            _events.emit(UiEvent.ShowMessage("Premium paket simüle edildi (test)"))
        }
    }

    override fun onCleared() {
        super.onCleared()
        billingManager.destroy()
    }
}
