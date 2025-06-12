package com.lowbyte.battery.animation.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.ActivityProBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.SKU_MONTHLY
import com.lowbyte.battery.animation.utils.AnimationUtils.SKU_WEEKLY
import com.lowbyte.battery.animation.utils.AnimationUtils.SKU_YEARLY
import com.lowbyte.battery.animation.utils.AppPreferences

class ProActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProBinding
    private lateinit var billingClient: BillingClient
    private var selectedPlanSku: String? = SKU_WEEKLY
    private lateinit var preferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = AppPreferences.getInstance(this)

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
                    Log.d("BillingForce", "Billing response via custom listener")

                    for (purchase in purchases) {
                        Log.d("BillingForce", "Billing loop via custom listener")

                        handlePurchase(purchase)
                    }
                }
            }
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("Billing", "Setup finished")
                    loadAllPrices()
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d("Billing", "Disconnected")
            }
        })
    }

    private fun loadAllPrices() {
        val productList = listOf(SKU_WEEKLY, SKU_MONTHLY, SKU_YEARLY).map { sku ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(sku)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            Log.d("BillingDebug", "Returned Product Count: ${productDetailsList.size}")
            productDetailsList.forEach {
                Log.d("BillingDebug", "Received Product ID: ${it.productId}")
            }

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (productDetails in productDetailsList) {
                    val productId = productDetails.productId
                    val offerDetailsList = productDetails.subscriptionOfferDetails

                    if (offerDetailsList.isNullOrEmpty()) {
                        Log.e("BillingDebug", "❌ No offers found for $productId")
                        continue
                    }

                    // Find first offer that has pricing phases
                    val validOffer =
                        offerDetailsList.firstOrNull { it.pricingPhases.pricingPhaseList?.isNotEmpty() == true }

                    if (validOffer == null) {
                        Log.e(
                            "BillingDebug",
                            "❌ No valid pricing phase in any offer for $productId"
                        )
                        continue
                    }

                    val pricingPhase = validOffer.pricingPhases.pricingPhaseList.firstOrNull()
                    val formattedPrice = pricingPhase?.formattedPrice ?: "—"

                    Log.d(
                        "BillingDebug", """
                ▶ Product ID: $productId
                ▶ Price: $formattedPrice
                ▶ OfferToken: ${validOffer.offerToken}
                ▶ BasePlanId: ${validOffer.basePlanId}
                """.trimIndent()
                    )

                    try {
                        runOnUiThread {
                            when (productId) {
                                SKU_WEEKLY -> binding.priceWeekly.text = formattedPrice
                                SKU_MONTHLY -> binding.priceMonthly.text = formattedPrice
                                SKU_YEARLY -> binding.priceYearly.text = formattedPrice
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("BillingUIError", "UI update failed for $productId", e)
                    }
                }
            } else {
                Log.e(
                    "BillingError",
                    "Failed to fetch product details: ${billingResult.debugMessage}"
                )
            }
        }
    }
    private fun setupListeners() {
        binding.viewWeekly.setOnClickListener {
            highlightSelection("weekly")
            selectedPlanSku = SKU_WEEKLY
        }

        binding.viewMonthly.setOnClickListener {
            highlightSelection("monthly")
            selectedPlanSku = SKU_MONTHLY
        }

        binding.viewYearly.setOnClickListener {
            highlightSelection("yearly")
            selectedPlanSku = SKU_YEARLY
        }

        binding.llPremiumRestore.setOnClickListener {
            openUrl(this, getString(R.string.restore_sub_url))
        }
        binding.closeScreen.setOnClickListener {
            finish()
        }

        binding.tvPremiumTermOfUse.setOnClickListener {
            openUrl(this, getString(R.string.terms_of_service_url))
        }

        binding.tvPremiumPrivacyPolicy.setOnClickListener {
            openUrl(this, getString(R.string.privacy_policy_url))
        }

        binding.buyNow.setOnClickListener {
            if (selectedPlanSku != null) {
                launchPurchase(selectedPlanSku!!)
            } else {
                Toast.makeText(this, "Please select a plan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchPurchase(sku: String) {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
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
                    )
                    .build()
                billingClient.launchBillingFlow(this, billingFlowParams)
            } else {
                Toast.makeText(this, "Product not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        val sku = purchase.products.firstOrNull() ?: return
        saveSubscription(sku)
        Toast.makeText(this, "Subscribed to $sku", Toast.LENGTH_SHORT).show()
    }

    private fun saveSubscription(sku: String) {
        preferences.setString("active_subscription", sku)
    }

    private fun openUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Unable to open URL", Toast.LENGTH_SHORT).show()
        }
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
}