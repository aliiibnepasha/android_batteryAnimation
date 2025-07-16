package com.lowbyte.battery.animation.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.billingclient.api.*
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.MyApplication
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.ActivityProBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.SKU_MONTHLY
import com.lowbyte.battery.animation.utils.AnimationUtils.SKU_WEEKLY
import com.lowbyte.battery.animation.utils.AnimationUtils.SKU_YEARLY
import com.lowbyte.battery.animation.utils.AnimationUtils.openUrl
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class ProActivity : BaseActivity() {

    private lateinit var binding: ActivityProBinding
    private lateinit var billingClient: BillingClient
    private var selectedPlanSku: String? = SKU_WEEKLY
    private lateinit var preferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = AppPreferences.getInstance(this)
        MyApplication.enableOpenAd(false)
        FirebaseAnalyticsUtils.logScreenView(this, "ProScreen")
        FirebaseAnalyticsUtils.startScreenTimer("ProScreen")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupBillingClient()
        setupListeners()
        highlightSelection("weekly")
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) handlePurchase(purchase)
                }
            }.build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    loadAllPrices()
                    checkExistingSubscriptions() // ✅ add this call
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d("Billing", "Disconnected")
            }
        })
    }

    private fun checkExistingSubscriptions() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                if (purchasesList.isNullOrEmpty()) {
                    Log.d("Billing", "No active subscriptions found.")
                    return@queryPurchasesAsync
                }
                for (purchase in purchasesList) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        if (!purchase.isAcknowledged) {
                            acknowledgePurchase(purchase)
                        }
                        // Process the active subscription (even if already acknowledged)
                        handlePurchase(purchase)
                    } else {
                        Log.d("Billing", "Purchase not in PURCHASED state: ${purchase.purchaseState}")
                    }
                }

            } else {
                FirebaseAnalyticsUtils.logClickEvent(
                    this,
                    "purchase_check_error",
                    mapOf("message" to billingResult.debugMessage)
                )
                Log.e("Billing", "Error checking purchases: ${billingResult.debugMessage}")
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d("Billing", "Purchase acknowledged successfully")
            } else {
                Log.e("Billing", "Failed to acknowledge purchase: ${billingResult.debugMessage}")
            }
        }
    }

    private fun loadAllPrices() {
        val productList = listOf(SKU_WEEKLY, SKU_MONTHLY, SKU_YEARLY).map { sku ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(sku)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (productDetails in productDetailsList) {
                    val pricing = productDetails.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: continue
                    Log.d("purchasedApp","products ${productDetails.productId}  / $pricing ")
                    runOnUiThread {
                        when (productDetails.productId) {
                            SKU_WEEKLY -> binding.priceWeekly.text = pricing
                            SKU_MONTHLY -> binding.priceMonthly.text = pricing
                            SKU_YEARLY -> binding.priceYearly.text = pricing
                        }
                    }
                }
            } else {
                FirebaseAnalyticsUtils.logClickEvent(this, "billing_error", mapOf("message" to billingResult.debugMessage))
            }
        }
    }

    private fun setupListeners() {
        binding.viewWeekly.setOnClickListener {
            highlightSelection("weekly")
            selectedPlanSku = SKU_WEEKLY
            FirebaseAnalyticsUtils.logClickEvent(this, "select_subscription_plan", mapOf("plan" to "weekly"))
        }

        binding.viewMonthly.setOnClickListener {
            highlightSelection("monthly")
            selectedPlanSku = SKU_MONTHLY
            FirebaseAnalyticsUtils.logClickEvent(this, "select_subscription_plan", mapOf("plan" to "monthly"))
        }

        binding.viewYearly.setOnClickListener {
            highlightSelection("yearly")
            selectedPlanSku = SKU_YEARLY
            FirebaseAnalyticsUtils.logClickEvent(this, "select_subscription_plan", mapOf("plan" to "yearly"))
        }

        binding.llPremiumRestore.setOnClickListener {
            openUrl(this, getString(R.string.restore_sub_url))
            FirebaseAnalyticsUtils.logClickEvent(this, "click_restore", mapOf("screen" to "ProScreen"))
        }

        binding.closeScreen.setOnClickListener {
            finish()
        }

        binding.tvPremiumTermOfUse.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_terms", null)
            openUrl(this, getString(R.string.terms_of_service_url))
        }

        binding.tvPremiumPrivacyPolicy.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_privacy", null)
            openUrl(this, getString(R.string.privacy_policy_url))
        }

        binding.buyNow.setOnClickListener {
            if (selectedPlanSku != null) {
                FirebaseAnalyticsUtils.logClickEvent(this, "click_buy_now", mapOf("selected_plan" to selectedPlanSku!!))
                launchPurchase(selectedPlanSku!!)
            } else {
                Toast.makeText(this, getString(R.string.please_select_a_plan), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchPurchase(sku: String) {
        val params = QueryProductDetailsParams.newBuilder().setProductList(
            listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(sku)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
        ).build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList[0]
                val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return@queryProductDetailsAsync
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .setOfferToken(offerToken)
                                .build()
                        )
                    ).build()
                billingClient.launchBillingFlow(this, billingFlowParams)
            } else {
                FirebaseAnalyticsUtils.logClickEvent(this, "billing_error", mapOf("message" to billingResult.debugMessage))
                Toast.makeText(this, getString(R.string.subscription_not_available), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        val sku = purchase.products.firstOrNull() ?: return
        saveSubscription(sku)
        FirebaseAnalyticsUtils.logClickEvent(this, "purchase_success", mapOf("sku" to sku))
        Toast.makeText(this, "Subscribed to $sku", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun saveSubscription(sku: String) {
        preferences.isProUser = true
        preferences.setString("active_subscription", sku)
    }

    private fun highlightSelection(plan: String) {
        val selectedColor = resources.getColor(R.color.text_color_pro)
        val defaultColor = resources.getColor(R.color.blue_app_color)

        binding.titleYearly.setTextColor(if (plan == "yearly") selectedColor else defaultColor)
        binding.priceYearly.setTextColor(if (plan == "yearly") selectedColor else defaultColor)
        binding.offerYearly.setTextColor(if (plan == "yearly") selectedColor else defaultColor)

        binding.titleMonthly.setTextColor(if (plan == "monthly") selectedColor else defaultColor)
        binding.priceMonthly.setTextColor(if (plan == "monthly") selectedColor else defaultColor)
        binding.offerMonthly.setTextColor(if (plan == "monthly") selectedColor else defaultColor)

        binding.titleWeekly.setTextColor(if (plan == "weekly") selectedColor else defaultColor)
        binding.priceWeekly.setTextColor(if (plan == "weekly") selectedColor else defaultColor)
        binding.offerWeekly.setTextColor(if (plan == "weekly") selectedColor else defaultColor)
    }

    override fun onPause() {
        super.onPause()
        FirebaseAnalyticsUtils.stopScreenTimer(this, "ProScreen")
    }

    override fun onDestroy() {
        MyApplication.enableOpenAd(true)
        super.onDestroy()
    }
}