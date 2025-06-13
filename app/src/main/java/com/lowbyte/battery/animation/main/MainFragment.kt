package com.lowbyte.battery.animation.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.activity.ProActivity
import com.lowbyte.battery.animation.activity.SettingsActivity
import com.lowbyte.battery.animation.ads.BannerAdHelper
import com.lowbyte.battery.animation.databinding.FragmentMainBinding
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class MainFragment : Fragment(R.layout.fragment_main) {

    private lateinit var binding: FragmentMainBinding
    private var doubleBackPressedOnce = false
    private lateinit var preferences: AppPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentMainBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        preferences = AppPreferences.getInstance(requireContext())
        FirebaseAnalyticsUtils.logScreenView(this, "main_screen")

        val navHostFragment = childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

        loadBannerAd()

        binding.ifvSetting.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(requireContext(), "click_settings")
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        binding.ifvPro.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(requireContext(), "click_pro")
            startActivity(Intent(requireContext(), ProActivity::class.java))
        }

        // Handle back press logic
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (navController.currentDestination?.id in listOf(
                    R.id.navigation_view_all_emoji,
                    R.id.navigation_view_all_widget,
                    R.id.navigation_view_all_animation)
            ) {
                navController.navigateUp()
            } else {
                if (doubleBackPressedOnce) {
                    FirebaseAnalyticsUtils.logClickEvent(requireContext(), "double_back_exit_confirm")
                    requireActivity().finish()
                } else {
                    doubleBackPressedOnce = true
                    FirebaseAnalyticsUtils.logClickEvent(requireContext(), "double_back_exit_attempt")
                    Toast.makeText(requireContext(), getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT).show()
                    Handler(Looper.getMainLooper()).postDelayed({ doubleBackPressedOnce = false }, 2000)
                }
            }
        }

        binding.ibBackButton.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(requireContext(), "click_nav_item_back")
            navController.navigateUp()
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val screenName = when (destination.id) {
                R.id.navigation_home -> {
                    binding.tvTitle.text = getString(R.string.title_home)
                    binding.ibBackButton.visibility = View.INVISIBLE
                    "home_screen"
                }
                R.id.navigation_customize -> {
                    binding.tvTitle.text = getString(R.string.menu_customize)
                    binding.ibBackButton.visibility = View.INVISIBLE
                    "customize_screen"
                }
                R.id.navigation_island -> {
                    binding.tvTitle.text = getString(R.string.menu_dynamic_island)
                    binding.ibBackButton.visibility = View.INVISIBLE
                    "island_screen"
                }
                R.id.navigation_view_all_emoji -> {
                    binding.tvTitle.text = getString(R.string.view_all_battery_emoji)
                    binding.ibBackButton.visibility = View.VISIBLE
                    "view_all_emoji_screen"
                }
                R.id.navigation_view_all_widget -> {
                    binding.tvTitle.text = getString(R.string.view_all_battery_widget)
                    binding.ibBackButton.visibility = View.VISIBLE
                    "view_all_widget_screen"
                }
                R.id.navigation_view_all_animation -> {
                    binding.tvTitle.text = getString(R.string.view_all_battery_animation)
                    binding.ibBackButton.visibility = View.VISIBLE
                    "view_all_animation_screen"
                }
                else -> null
            }

            screenName?.let {
                FirebaseAnalyticsUtils.logClickEvent(requireContext(), "nav_screen_view", mapOf("screen_name" to it))
            }
        }
        askNotificationPermission()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted && shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            showRationaleDialog()
        } else if (!isGranted) {
            Toast.makeText(requireContext(), "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun askNotificationPermission(forceShow: Boolean = false) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (isGranted) return

            if (forceShow || !hasShownNotificationDialogBefore()) {
                showPermissionDialog()
            }
        }
    }

    private fun showPermissionDialog() {
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Enable Notifications")
            .setMessage("Stay updated with latest battery animations and updates by enabling notifications.")
            .setPositiveButton("Allow") { _, _ ->
                markNotificationDialogShown()
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton("Later") { dialogInterface, _ ->
                markNotificationDialogShown()
                dialogInterface.dismiss()
            }
            .setCancelable(false)
            .create()
        dialog.show()
    }

    private fun showRationaleDialog() {
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Why We Need Notification Access")
            .setMessage("We use notifications to alert you of new battery animations and updates. Please allow it from app settings if you want to stay informed.")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun hasShownNotificationDialogBefore(): Boolean {
        return requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .getBoolean("notification_dialog_shown", false)
    }

    private fun markNotificationDialogShown() {
        requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .edit {
                putBoolean("notification_dialog_shown", true)
            }
    }

    private fun loadBannerAd() {
        BannerAdHelper.loadBannerAd(
            context = requireContext(),
            container = binding.bannerAdHome,
            isProUser = preferences.isProUser
        )
    }

    override fun onResume() {
        if (preferences.isProUser) {
            binding.bannerAdHome.visibility = View.GONE
            binding.ifvPro.visibility = View.GONE
        }
        super.onResume()
    }
}