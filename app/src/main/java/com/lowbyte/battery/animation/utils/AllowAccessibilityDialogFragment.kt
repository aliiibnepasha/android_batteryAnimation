package com.lowbyte.battery.animation.utils

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lowbyte.battery.animation.ads.BannerAdHelper
import com.lowbyte.battery.animation.databinding.ActivityAllowAccecibilityBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.getBannerPermissionId
import com.lowbyte.battery.animation.utils.AnimationUtils.isBannerPermissionSettings
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class AllowAccessibilityDialogFragment : BottomSheetDialogFragment() {

    private var _binding: ActivityAllowAccecibilityBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferences: AppPreferences

    // Callback interface
    interface OnAccessibilitySettingClickListener {
        fun onAccessibilitySettingClicked()
    }

    private var listener: OnAccessibilitySettingClickListener? = null

    fun setOnAccessibilitySettingClickListener(listener: OnAccessibilitySettingClickListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            com.google.android.material.R.style.Theme_Material3_Light_BottomSheetDialog
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityAllowAccecibilityBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferences = AppPreferences.getInstance(requireContext())

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.buttonForSetting.setOnClickListener {
            // Notify parent via listener
            listener?.onAccessibilitySettingClicked()
            FirebaseAnalyticsUtils.logClickEvent(
                requireContext(),
                "accessibility_to_setting",
                null
            )
            dismiss()

        }

        loadBannerAd(requireActivity())
    }

    private fun loadBannerAd(context: Activity) {
        if (preferences.isProUser || !isBannerPermissionSettings) {
            binding.bannerAdPermission.visibility = View.GONE
            return
        }

        BannerAdHelper.loadBannerAd(
            context = context,
            container = binding.bannerAdPermission,
            bannerAdId = getBannerPermissionId(false),
            isCollapsable = false,
            isProUser = preferences.isProUser,
            isBannerPermissionSettings
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}