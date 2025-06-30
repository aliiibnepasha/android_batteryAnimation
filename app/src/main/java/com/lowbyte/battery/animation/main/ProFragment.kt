package com.lowbyte.battery.animation.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.databinding.FragmentProBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.SKU_MONTHLY
import com.lowbyte.battery.animation.utils.AnimationUtils.SKU_WEEKLY
import com.lowbyte.battery.animation.utils.AnimationUtils.SKU_YEARLY
import com.lowbyte.battery.animation.utils.AnimationUtils.openUrl
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class ProFragment : Fragment() {

    private lateinit var binding: FragmentProBinding
    private lateinit var billingClient: BillingClient
    private var selectedPlanSku: String? = SKU_WEEKLY
    private lateinit var preferences: AppPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Log.d("backPress", "closedonSplash")
        }

        binding = FragmentProBinding.inflate(inflater, container, false)
        preferences = AppPreferences.getInstance(requireContext())

        FirebaseAnalyticsUtils.logScreenView(requireActivity(), "ProScreen")
        FirebaseAnalyticsUtils.startScreenTimer("ProScreen")

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupBillingClient()
        setupListeners()
        highlightSelection("weekly")

        return binding.root
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(requireContext()).enablePendingPurchases()
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) handlePurchase(purchase)
                }
            }.build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    loadAllPrices()
                    checkExistingSubscriptions()
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d("Billing", "Disconnected")
            }
        })
    }

    private fun checkExistingSubscriptions() {
        val params =
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()

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
                        handlePurchase(purchase)
                    } else {
                        Log.d(
                            "Billing", "Purchase not in PURCHASED state: ${purchase.purchaseState}"
                        )
                    }
                }

            } else {
                FirebaseAnalyticsUtils.logClickEvent(
                    requireContext(),
                    "purchase_check_error",
                    mapOf("message" to billingResult.debugMessage)
                )
                Log.e("Billing", "Error checking purchases: ${billingResult.debugMessage}")
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams =
            AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()

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
            QueryProductDetailsParams.Product.newBuilder().setProductId(sku)
                .setProductType(BillingClient.ProductType.SUBS).build()
        }

        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (productDetails in productDetailsList) {
                    val pricing =
                        productDetails.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                            ?: continue
                    requireActivity().runOnUiThread {
                        when (productDetails.productId) {
                            SKU_WEEKLY -> binding.priceWeekly.text = pricing
                            SKU_MONTHLY -> binding.priceMonthly.text = pricing
                            SKU_YEARLY -> binding.priceYearly.text = pricing
                        }
                    }
                }
            } else {
                FirebaseAnalyticsUtils.logClickEvent(
                    requireContext(),
                    "billing_error",
                    mapOf("message" to billingResult.debugMessage)
                )
            }
        }
    }

    private fun setupListeners() {
        binding.viewWeekly.setOnClickListener {
            highlightSelection("weekly")
            selectedPlanSku = SKU_WEEKLY
            FirebaseAnalyticsUtils.logClickEvent(
                requireContext(), "select_subscription_plan", mapOf("plan" to "weekly")
            )
        }

        binding.viewMonthly.setOnClickListener {
            highlightSelection("monthly")
            selectedPlanSku = SKU_MONTHLY
            FirebaseAnalyticsUtils.logClickEvent(
                requireContext(), "select_subscription_plan", mapOf("plan" to "monthly")
            )
        }

        binding.viewYearly.setOnClickListener {
            highlightSelection("yearly")
            selectedPlanSku = SKU_YEARLY
            FirebaseAnalyticsUtils.logClickEvent(
                requireContext(), "select_subscription_plan", mapOf("plan" to "yearly")
            )
        }

        binding.llPremiumRestore.setOnClickListener {
            openUrl(requireContext(), getString(R.string.restore_sub_url))
            FirebaseAnalyticsUtils.logClickEvent(
                requireContext(), "click_restore", mapOf("screen" to "ProScreen")
            )
        }

        binding.closeScreen.setOnClickListener {
            val destination = if (preferences.isFirstRun) {
                preferences.serviceRunningFlag = false
                preferences.isFirstRun = false
                R.id.action_pro_to_language
            } else {
                R.id.action_pro_to_main
            }

            AdManager.showInterstitialAd(requireActivity(), false) {
                if (isAdded && findNavController().currentDestination?.id == R.id.proFragment) {
                    findNavController().navigate(destination)
                } else {
                    Log.w("Navigation", "Attempted to navigate from incorrect fragment")
                }
            }
        }

        binding.tvPremiumTermOfUse.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(requireContext(), "click_terms", null)
            openUrl(requireContext(), getString(R.string.terms_of_service_url))
        }

        binding.tvPremiumPrivacyPolicy.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(requireContext(), "click_privacy", null)
            openUrl(requireContext(), getString(R.string.privacy_policy_url))
        }

        binding.buyNow.setOnClickListener {
            if (selectedPlanSku != null) {
                FirebaseAnalyticsUtils.logClickEvent(
                    requireContext(), "click_buy_now", mapOf("selected_plan" to selectedPlanSku!!)
                )
                launchPurchase(selectedPlanSku!!)
            } else {
                Toast.makeText(
                    requireContext(), getString(R.string.please_select_a_plan), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun launchPurchase(sku: String) {
        val params = QueryProductDetailsParams.newBuilder().setProductList(
            listOf(
                QueryProductDetailsParams.Product.newBuilder().setProductId(sku)
                    .setProductType(BillingClient.ProductType.SUBS).build()
            )
        ).build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList[0]
                val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
                    ?: return@queryProductDetailsAsync
                val billingFlowParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails).setOfferToken(offerToken).build()
                    )
                ).build()
                billingClient.launchBillingFlow(requireActivity(), billingFlowParams)
            } else {
                FirebaseAnalyticsUtils.logClickEvent(
                    requireContext(),
                    "billing_error",
                    mapOf("message" to billingResult.debugMessage)
                )
                Toast.makeText(
                    requireContext(),
                    getString(R.string.subscription_not_available),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        val sku = purchase.products.firstOrNull() ?: return
        saveSubscription(sku)
        FirebaseAnalyticsUtils.logClickEvent(
            requireContext(), "purchase_success", mapOf("sku" to sku)
        )
        Toast.makeText(requireContext(), "Subscribed to $sku", Toast.LENGTH_SHORT).show()
        requireActivity().finish()
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
    }
}