package com.suanmesgulum.app.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google Play Billing Manager.
 * Chatterbox gelişmiş ses paketi için In-App Purchase yönetimi.
 *
 * Ürün: "premium_voice_pack" - 2.99$ (tek seferlik)
 * İleride abonelik modeli de eklenebilir.
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BillingManager"
        const val PRODUCT_PREMIUM_VOICE = "premium_voice_pack"
    }

    /** Satın alma durumu */
    sealed class PurchaseState {
        object NotPurchased : PurchaseState()
        object Purchased : PurchaseState()
        object Pending : PurchaseState()
        data class Error(val message: String) : PurchaseState()
    }

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.NotPurchased)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    private var billingClient: BillingClient? = null

    /**
     * Billing client'ı başlat ve bağlan.
     */
    fun initialize() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "Billing bağlantısı kuruldu")
                    queryProducts()
                    queryPurchases()
                } else {
                    Log.e(TAG, "Billing bağlantı hatası: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing bağlantısı kesildi")
            }
        })
    }

    /**
     * Ürün bilgilerini sorgula.
     */
    private fun queryProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_PREMIUM_VOICE)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val details = productDetailsList.firstOrNull()
                _productDetails.value = details
                Log.i(TAG, "Ürün bilgisi alındı: ${details?.title}")
            } else {
                Log.e(TAG, "Ürün sorgulama hatası: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Mevcut satın almaları sorgula.
     */
    private fun queryPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient?.queryPurchasesAsync(params) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val premiumPurchase = purchasesList.firstOrNull { purchase ->
                    purchase.products.contains(PRODUCT_PREMIUM_VOICE)
                }

                if (premiumPurchase != null &&
                    premiumPurchase.purchaseState == Purchase.PurchaseState.PURCHASED
                ) {
                    _purchaseState.value = PurchaseState.Purchased
                    // Acknowledge edilmemişse acknowledge et
                    if (!premiumPurchase.isAcknowledged) {
                        acknowledgePurchase(premiumPurchase)
                    }
                    Log.i(TAG, "Premium paket aktif")
                }
            }
        }
    }

    /**
     * Satın alma akışını başlat.
     */
    fun launchPurchaseFlow(activity: Activity) {
        val details = _productDetails.value
        if (details == null) {
            Log.e(TAG, "Ürün detayları henüz yüklenmedi")
            _purchaseState.value = PurchaseState.Error("Ürün bilgisi yüklenemedi")
            return
        }

        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .apply {
                    offerToken?.let { setOfferToken(it) }
                }
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val billingResult = billingClient?.launchBillingFlow(activity, billingFlowParams)
        if (billingResult?.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.e(TAG, "Satın alma akışı başlatılamadı: ${billingResult?.debugMessage}")
            _purchaseState.value = PurchaseState.Error(
                billingResult?.debugMessage ?: "Bilinmeyen hata"
            )
        }
    }

    /**
     * Satın alma güncelleme callback'i.
     */
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.i(TAG, "Kullanıcı satın almayı iptal etti")
                _purchaseState.value = PurchaseState.NotPurchased
            }
            else -> {
                Log.e(TAG, "Satın alma hatası: ${billingResult.debugMessage}")
                _purchaseState.value = PurchaseState.Error(
                    billingResult.debugMessage ?: "Satın alma hatası"
                )
            }
        }
    }

    /**
     * Satın almayı işle.
     */
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (purchase.products.contains(PRODUCT_PREMIUM_VOICE)) {
                _purchaseState.value = PurchaseState.Purchased
                Log.i(TAG, "Premium paket satın alındı!")

                if (!purchase.isAcknowledged) {
                    acknowledgePurchase(purchase)
                }
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            _purchaseState.value = PurchaseState.Pending
            Log.i(TAG, "Satın alma beklemede")
        }
    }

    /**
     * Satın almayı onaylama (acknowledge).
     * Google Play, 3 gün içinde acknowledge edilmeyen satın almaları iptal eder.
     */
    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.i(TAG, "Satın alma onaylandı")
            } else {
                Log.e(TAG, "Satın alma onaylama hatası: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Premium satın alma durumunu test ortamında simüle et.
     * DEBUG modunda kullanılır.
     */
    fun simulatePurchase() {
        _purchaseState.value = PurchaseState.Purchased
        Log.i(TAG, "Satın alma simüle edildi (test modu)")
    }

    /**
     * Billing client'ı kapat.
     */
    fun destroy() {
        billingClient?.endConnection()
        billingClient = null
    }
}
