package com.lowbyte.battery.animation.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.ActivityProBinding
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.google.firebase.analytics.FirebaseAnalytics
import com.lowbyte.battery.animation.model.ModelSubscription
import com.lowbyte.battery.animation.utils.AnimationUtils.lifeTimePorductId
import com.lowbyte.battery.animation.utils.AnimationUtils.monthlySubId
import com.lowbyte.battery.animation.utils.AnimationUtils.showCustomToast
import com.lowbyte.battery.animation.utils.AnimationUtils.yearlySubId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProBinding
    private lateinit var billingClient: BillingClient
    lateinit var firebaseAnalytics: FirebaseAnalytics
    private var selectedProductId = yearlySubId
    private val productDetailsMap = mutableMapOf<String, ProductDetails>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSpannables()
        setupBillingClient()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.viewYearly.setOnClickListener {
            binding.titleYearly.setTextColor(resources.getColor(R.color.text_color_pro))
            binding.priceYearly.setTextColor(resources.getColor(R.color.text_color_pro))
            binding.offerYearly.setTextColor(resources.getColor(R.color.text_color_pro))
        }

        binding.viewMonthly.setOnClickListener {
            binding.titleMonthly.setTextColor(resources.getColor(R.color.blue_app_color))
            binding.priceMonthly.setTextColor(resources.getColor(R.color.blue_app_color))
            binding.offerMonthly.setTextColor(resources.getColor(R.color.blue_app_color))
        }

        binding.viewWeekly.setOnClickListener {
            binding.titleWeekly.setTextColor(resources.getColor(R.color.blue_app_color))
            binding.priceWeekly.setTextColor(resources.getColor(R.color.blue_app_color))
            binding.offerWeekly.setTextColor(resources.getColor(R.color.blue_app_color))


        }



        binding.llPremiumRestore.setOnClickListener {
            openUrl(
                this,
                getString(R.string.terms_of_service_url)
            )
        }

         binding.tvPremiumTermOfUse.setOnClickListener {
            openUrl(
                this,
                getString(R.string.terms_of_service_url)
            )
        }



        binding.tvPremiumPrivacyPolicy.setOnClickListener {
            openUrl(
                this,
                getString(R.string.privacy_policy_url)
            )
        }
    }

    //Billing
    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .setListener(subscribeUpdateListener)
            .enablePendingPurchases()
            .build()
        connectBillingClient()
    }
    private fun setSpannables() {

        val text = getString(R.string.restore_purchase)
        val spannableString = SpannableString(text)
        spannableString.setSpan(UnderlineSpan(), 0, text.length, 0)
        binding.llPremiumRestore.text = spannableString

        val terms = getString(R.string.terms_of_service)
        val spannableStringterms = SpannableString(terms)
        spannableStringterms.setSpan(UnderlineSpan(), 0, terms.length, 0)
        binding.tvPremiumTermOfUse.text = spannableStringterms

        val privacy_policies = getString(R.string.privacy_policy)
        val spannableStringprivacy_policies = SpannableString(privacy_policies)
        spannableStringprivacy_policies.setSpan(UnderlineSpan(), 0, privacy_policies.length, 0)
        binding.tvPremiumPrivacyPolicy.text = spannableStringprivacy_policies

    }

    private fun connectBillingClient() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                Log.e("Billing", "Billing service disconnected")
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("Billing", "Billing service connected")
                    queryProductDetails()
                } else {
                    showCustomToast("Failed to connect to billing service: ${billingResult.debugMessage}")
                }
            }
        })
    }

    private fun queryProductDetails() {

        val subsProductList = listOf(

            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(monthlySubId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(yearlySubId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        // Define in-app products
        val inAppProductList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(lifeTimePorductId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        lifecycleScope.launch(Dispatchers.IO) {
            val updatedSubscriptionDataList = mutableListOf<ModelSubscription>()

            // Query subscriptions
            val subsParams = QueryProductDetailsParams.newBuilder()
                .setProductList(subsProductList)
                .build()
            val subsResult = billingClient.queryProductDetails(subsParams)

            if (subsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                subsResult.productDetailsList?.forEach { productDetails ->
                    productDetailsMap[productDetails.productId] = productDetails


                    val yearlyPrice = productDetails.subscriptionOfferDetails
                        ?.firstOrNull { it.offerId != "3-days-trial" } // Ignore the trial offer
                        ?.pricingPhases?.pricingPhaseList
                        ?.firstOrNull { it.recurrenceMode == 1 } // Get actual subscription price
                        ?.formattedPrice ?: "N/A"

                    val price = productDetails.subscriptionOfferDetails
                        ?.flatMap { it.pricingPhases.pricingPhaseList } // Get all pricing phases
                        ?.firstOrNull { it.recurrenceMode == 1 } // Find the one with recurrenceMode = 1 (actual subscription price)
                        ?.formattedPrice ?: "N/A" // Use fallback in case no valid price is found



                    val (duration, label) = getSubscriptionDurationAndLabel(productDetails.productId)
                    updatedSubscriptionDataList.add(
                        ModelSubscription(
                            duration,
                            label,
                            if (productDetails.productId == yearlySubId) yearlyPrice else price,
                            productDetails.productId
                        )
                    )
                }
            }

            // Query in-app products
            val inAppParams = QueryProductDetailsParams.newBuilder()
                .setProductList(inAppProductList)
                .build()
            val inAppResult = billingClient.queryProductDetails(inAppParams)

            if (inAppResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                var yearlyPrice=""
                inAppResult.productDetailsList?.forEach { productDetails ->
                    productDetailsMap[productDetails.productId] = productDetails

                    val pricingPhases = productDetailsMap["yearly_sub"]
                        ?.subscriptionOfferDetails
                        ?.find { it.basePlanId == "yearly-base-plan" }
                        ?.pricingPhases
                        ?.pricingPhaseList

                    yearlyPrice = when {
                        pricingPhases == null || pricingPhases.isEmpty() -> "N/A"
                        pricingPhases.size > 1 -> pricingPhases[1].formattedPrice
                        else -> pricingPhases[0].formattedPrice
                    }

                    //  yearlyPrice = productDetailsMap.get("yearly_sub")?.subscriptionOfferDetails?.find { it.basePlanId=="yearly-base-plan" }?.pricingPhases?.pricingPhaseList?.get(0)?.formattedPrice.toString()


                    val price = productDetails.oneTimePurchaseOfferDetails?.formattedPrice ?: ""

                    val (duration, label) = getSubscriptionDurationAndLabel(productDetails.productId)
                    updatedSubscriptionDataList.add(
                        ModelSubscription(
                            duration,
                            label,
                            price,
                            productDetails.productId
                        )
                    )
                }
                withContext(Dispatchers.Main) {
                    val text = binding.tvPrice.text.toString()
                    val priceText = text.replace("%dvalue%", yearlyPrice)
                    binding.tvPrice.text = priceText
                }

            }

            val sortedSubscriptionDataList =
                updatedSubscriptionDataList.sortedWith(compareBy { subscription ->
                    when (subscription.productId) {
                        monthlySubId -> 1
                        yearlySubId -> 2
                        lifeTimePorductId -> 3
                        else -> Int.MAX_VALUE
                    }
                })

            // Update the UI with the sorted results
            withContext(Dispatchers.Main) {
                try {
//                    initAdapter(sortedSubscriptionDataList)
                } catch (e: Exception) {
                    e.message?.let { showCustomToast(it) }
                }
//                dismissProgressDialog()
            }
        }
    }

    private fun initiateSubscribe(productId: String) {
        val productDetails = productDetailsMap[productId]
        if (productDetails != null) {
            val offerToken = productDetails.subscriptionOfferDetails?.first()?.offerToken
            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(offerToken ?: "")
                            .build()
                    )
                )
                .build()
            val billingResult = billingClient.launchBillingFlow(this, flowParams)
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                showCustomToast(resources.getString(R.string.string_something_went_wrong_please_try_again))
            }
        } else {
            showCustomToast(resources.getString(R.string.string_something_went_wrong_please_try_again))
        }
    }

    private val subscribeUpdateListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                when (purchase.products.firstOrNull()) {
                    lifeTimePorductId -> handleLifetimePurchase(purchase)
                    else -> handleSubscribe(purchase)
                }

            }
        }
    }

    private fun handleLifetimePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            logPurchaseSuccess()
            preferenceManager.saveSubscriptionActive(true)
            preferenceManager.saveLifeTimeAccessActive(true)
            if (!preferenceManager.isOnboardingFinished()) {
                val bundle = Bundle()
                bundle.putBoolean("comingFromOnboarding", true)

                navigate(
                    R.id.subscriptionFragment,
                    R.id.action_subscriptionFragmentAuth_to_localizeFragment2,
                    bundle
                )
            } else {
                finish()
            }
        } else {
            showCustomToast(resources.getString(R.string.string_something_went_wrong_please_try_again))
        }
    }

    private fun handleSubscribe(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            logSubscriptionSuccess()
            preferenceManager.saveSubscriptionActive(true)
            if (!preferenceManager.isOnboardingFinished()) {
                val bundle = Bundle()
                bundle.putBoolean("comingFromOnboarding", true)

                navigate(
                    R.id.subscriptionFragment,
                    R.id.action_subscriptionFragmentAuth_to_localizeFragment2,
                    bundle
                )
            } else {
               finish()
            }
        }
    }

    private fun initiatePurchase(productId: String) {
        val productDetails = productDetailsMap[productId]
        if (productDetails != null) {
            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )
                )
                .build()

            val billingResult = billingClient.launchBillingFlow(this, flowParams)
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                showCustomToast(resources.getString(R.string.string_something_went_wrong_please_try_again))
            }
        } else {
            showCustomToast(resources.getString(R.string.string_something_went_wrong_please_try_again))
        }
    }


    private fun getSubscriptionDurationAndLabel(productId: String): Pair<String, String> {
        return when (productId) {
            monthlySubId -> Pair("Monthly", "Most popular")
            yearlySubId -> Pair("Yearly", "Greatest Deal")
            lifeTimePorductId -> Pair("Lifetime Access", "Enterprise")
            else -> Pair("Unknown", "N/A")
        }
    }


    private fun openUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open URL", Toast.LENGTH_SHORT).show()
            Log.e("URL_OPEN_ERROR", "Error opening URL: $url", e)
        }
    }

    override fun onResume() {
        super.onResume()
    }

}