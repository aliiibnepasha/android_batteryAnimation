package com.lowbyte.battery.animation.utils

import android.app.Activity
import android.app.Dialog
import android.view.LayoutInflater
import android.view.ViewGroup
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.DialogGoProBinding

class RewardedDialogUtils(
    private val activity: Activity,
    private val isProUser: Boolean,
    private val isRewardedEnabled: Boolean
) {
    private var dialog: Dialog? = null
    private var binding: DialogGoProBinding? = null

    fun init(
        onWatchAd: () -> Unit,
        onGoPremium: () -> Unit
    ) {
        dialog = Dialog(activity).apply {
            binding = DialogGoProBinding.inflate(LayoutInflater.from(activity))
            setContentView(binding!!.root)
            setCancelable(false)
            window?.attributes?.windowAnimations = R.style.DialogAnimation
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            window?.setBackgroundDrawableResource(android.R.color.transparent)

            binding?.ivClose?.setOnClickListener { dismiss() }

            if (!isRewardedEnabled) {
                binding?.btnWatchAd?.visibility = android.view.View.GONE
                binding?.btnPremium?.visibility = android.view.View.VISIBLE
            } else {
                binding?.btnWatchAd?.visibility = android.view.View.VISIBLE
                binding?.btnPremium?.visibility = android.view.View.VISIBLE
                binding?.btnWatchAd?.setOnClickListener {
                    dismiss()
                    onWatchAd()
                }
            }

            binding?.btnPremium?.setOnClickListener {
                dismiss()
                onGoPremium()
            }
        }
    }

    fun show() {
        if (isProUser) return // No need to show for Pro users
        dialog?.show()
    }

    fun dismiss() {
        if (dialog?.isShowing == true) {
            dialog?.dismiss()
        }
    }
}
