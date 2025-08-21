package com.lowbyte.battery.animation.dialoge

import android.app.Activity
import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.ads.RewardedAdManager
import com.lowbyte.battery.animation.ads.RewardedAdManager.isInternetAvailable
import com.lowbyte.battery.animation.ads.RewardedAdManager.rewardedAdLoaded
import com.lowbyte.battery.animation.databinding.DialogGoProBinding
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

object RewardedDialogHandler {

    fun showRewardedDialog(
        context: Activity,
        preferences: AppPreferences,
        isSkipShow: Boolean,
        isRewardedEnabled: Boolean,
        onCompleted: (() -> Unit)? = null,
        onSkip: (() -> Unit)? = null
    ) {
        val dialog = Dialog(context)
        val binding = DialogGoProBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.setCancelable(false)
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.ivClose.setOnClickListener { dialog.dismiss() }

        if (!isRewardedEnabled) {
            binding.btnWatchAd.visibility = View.GONE
        } else {
            binding.btnWatchAd.visibility = View.VISIBLE

            if (isSkipShow){
                binding.tvHint.visibility = View.VISIBLE
            }else{
                binding.tvHint.visibility = View.INVISIBLE
            }
            binding.tvHint.setOnClickListener {
                dialog.dismiss()
                onSkip?.invoke()
            }

            binding.btnWatchAd.setOnClickListener {
                dialog.dismiss()
                if (isInternetAvailable(context)){
                    RewardedAdManager.showRewardedAd(
                        activity = context,
                        onRewardEarned = {
                            rewardedAdLoaded = false
                            preferences.setBoolean("RewardEarned", true)
                        },
                        onAdShown = {
                            rewardedAdLoaded = false
                            // Log analytics if needed
                        },
                        onAdDismissed = {
                            rewardedAdLoaded = false
                            preferences.isStatusBarEnabled = true
                            FirebaseAnalyticsUtils.logClickEvent(context, "reward_ad_dismissed", null)
                            onCompleted?.invoke()
                        }
                    )
                }else{
                    Toast.makeText(context, context.getString(R.string.internet_not_available), Toast.LENGTH_SHORT).show()
                }


            }
        }

        dialog.show()
    }
}
