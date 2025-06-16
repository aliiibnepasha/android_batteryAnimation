package com.lowbyte.battery.animation.activity

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams
import com.google.android.gms.ads.MobileAds
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.databinding.ActivitySplashBinding
import com.lowbyte.battery.animation.utils.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var preferences: AppPreferences
    private lateinit var billingClient: BillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SplashActivityLog", "onCreate called")
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = AppPreferences.getInstance(this)
        Log.d("SplashActivityLog", "Preferences initialized: isProUser=${preferences.isProUser}")

        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@SplashActivity) {
                Log.d("SplashActivityLog", "MobileAds initialized")
            }
        }

        AdManager.initializeAds(this)
        Log.d("SplashActivityLog", "AdManager initialized")

        checkSubscriptionStatus()
    }

    private fun checkSubscriptionStatus() {
        Log.d("SplashActivityLog", "Starting subscription status check")

        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener { billingResult, purchases ->
                Log.d("SplashActivityLog", "Billing listener called: ${billingResult.debugMessage}")
            }
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Log.d("SplashActivityLog", "Billing setup finished: ${billingResult.responseCode}")
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryActiveSubscriptions()
                } else {
                    Log.w("SplashActivityLog", "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w("SplashActivityLog", "Billing service disconnected")
            }
        })
    }

    private fun queryActiveSubscriptions() {
        Log.d("SplashActivityLog", "Querying active subscriptions...")

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchases ->
            Log.d("SplashActivityLog", "Query result: ${billingResult.responseCode}, ${billingResult.debugMessage}")

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d("SplashActivityLog", "Found ${purchases.size} purchase(s)")

                val isStillSubscribed = purchases.any { purchase ->
                    Log.d(
                        "SplashActivityLog",
                        "Purchase: state=${purchase.purchaseState}, acknowledged=${purchase.isAcknowledged}, products=${purchase.products}"
                    )
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED && purchase.isAcknowledged
                }

                preferences.isProUser = isStillSubscribed
                Log.d("SplashActivityLog", "isProUser set to $isStillSubscribed")

                if (!isStillSubscribed) {
                    preferences.setString("active_subscription", "")
                    Log.d("SplashActivityLog", "Cleared active_subscription preference")
                }
            } else {
                Log.e("SplashActivityLog", "Failed to query purchases: ${billingResult.debugMessage}")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("SplashActivityLog", "onResume called")
    }
}