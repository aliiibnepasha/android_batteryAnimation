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
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.lowbyte.battery.animation.MyApplication
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.activity.HowToUseActivity
import com.lowbyte.battery.animation.activity.ProActivity
import com.lowbyte.battery.animation.activity.SettingsActivity
import com.lowbyte.battery.animation.ads.BannerAdHelper
import com.lowbyte.battery.animation.ads.NativeBannerSizeHelper
import com.lowbyte.battery.animation.databinding.FragmentMainBinding
import com.lowbyte.battery.animation.model.NavItem
import com.lowbyte.battery.animation.utils.AnimationUtils.getBannerId
import com.lowbyte.battery.animation.utils.AnimationUtils.getNativeHomeId
import com.lowbyte.battery.animation.utils.AnimationUtils.isBannerHomeEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeHomeEnabled
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils
import kotlin.collections.contains

class MainFragment : Fragment(R.layout.fragment_main) {

    private lateinit var binding: FragmentMainBinding
    private var doubleBackPressedOnce = false
    private lateinit var preferences: AppPreferences
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment

    private var nativeHelper: NativeBannerSizeHelper? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentMainBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        preferences = AppPreferences.getInstance(requireContext())
        FirebaseAnalyticsUtils.logScreenView(this, "main_screen")
        MyApplication.enableOpenAd(true)

         navHostFragment = childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
         navController = navHostFragment.navController
        setupCustomBottomNav()

        //  binding.bottomNavigation.setupWithNavController(navController)

//        binding.bottomNavigation.setOnItemSelectedListener {
//            Log.d("navCustom", "Selected: ${it.itemId}")
//            when (it.itemId) {
//                R.id.navigation_customize -> {
//                    Log.d("navCustom", "Customize Reselected")
//                    val current = navController.currentDestination?.id
//                    if (current in listOf(R.id.navigation_view_all_emoji, R.id.navigation_view_all_widget, R.id.navigation_view_all_animation)) {
//                        navController.navigateUp()
//                        Log.d("navCustom", "Customize Reselected")
//                        FirebaseAnalyticsUtils.logClickEvent(requireContext(), "main_to_customize", null)
//
//                        navHostFragment.navController.navigate(R.id.navigation_customize)
//
//                    } else {
//                        FirebaseAnalyticsUtils.logClickEvent(requireContext(), "main_to_customize", null)
//
//                        Log.d("navCustom", "Customize Reselected r")
//                        navHostFragment.navController.navigate(R.id.navigation_customize)
//
//                    }
//                }
//
//                R.id.navigation_home -> {
//                    Log.d("navCustom", "Customize Reselected")
//                    val current = navController.currentDestination?.id
//                    if (current in listOf(
//                            R.id.navigation_view_all_emoji,
//                            R.id.navigation_view_all_widget,
//                            R.id.navigation_view_all_animation
//                        )
//                    ) {
//                        navController.navigateUp()
//                        Log.d("navCustom", "Customize Reselected")
//                        navHostFragment.navController.navigate(R.id.navigation_home)
//
//                    } else {
//                        Log.d("navCustom", "Customize Reselected r")
//                        navHostFragment.navController.navigate(R.id.navigation_home)
//
//                    }
//                }
//                 R.id.navigation_island -> {
//                     Log.d("navCustom", "Customize Reselected")
//                     val current = navController.currentDestination?.id
//                     if (current in listOf(R.id.navigation_view_all_emoji, R.id.navigation_view_all_widget, R.id.navigation_view_all_animation)) {
//                         navController.navigateUp()
//                         Log.d("navCustom", "Customize Reselected")
//                         navHostFragment.navController.navigate(R.id.navigation_island)
//
//                     } else {
//                         Log.d("navCustom", "Customize Reselected r")
//                         navHostFragment.navController.navigate(R.id.navigation_island)
//
//                     }
//                }
//
//
//
//
//
//
//
//            }
//            false // let navController handle navigation
//        }

        loadBannerAd()

        binding.ifvSetting.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(requireContext(), "click_settings")
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }
        binding.ibHowToUseButton.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(requireContext(), "click_how_to_use")
            startActivity(Intent(requireContext(), HowToUseActivity::class.java))
        }

          binding.ifvInfoRight.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(requireContext(), "click_how_to_use")
            startActivity(Intent(requireContext(), HowToUseActivity::class.java))
        }



        binding.ifvPro.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(requireContext(), "click_pro")
            startActivity(Intent(requireContext(), ProActivity::class.java))
        }

        // Handle back press logic
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val currentChildDestId = (childFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment)
                ?.navController?.currentDestination?.id

            when (currentChildDestId) {
                in listOf(
                    R.id.navigation_view_all_emoji,
                    R.id.navigation_view_all_widget,
                    R.id.navigation_view_all_animation
                ) -> {
                    Log.d("navCustom", "Back from child inner screen")
                    navController.navigateUp()
                }
                // If currently in Home child fragment, handle double back to exit
                R.id.navigation_home -> {
                    if (doubleBackPressedOnce) {
                        requireActivity().finishAffinity()
                        Log.d("navCustom", "Back Finish")
                    } else {
                        doubleBackPressedOnce = true
                        Toast.makeText(requireContext(), getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT).show()
                        Handler(Looper.getMainLooper()).postDelayed({
                            doubleBackPressedOnce = false
                        }, 2000)
                    }
                }
                // Else, just navigate up
                else -> {
                    navController.navigateUp()
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
                    setupCustomBottomNav(0,true)
                    binding.tvTitle.text = getString(R.string.title_home)
                    binding.ibBackButton.visibility = View.INVISIBLE
                    binding.ibHowToUseButton.visibility = View.VISIBLE
                    binding.ifvPro.visibility = View.VISIBLE
                    binding.ifvInfoRight.visibility = View.INVISIBLE
                    "home_screen"
                }
                R.id.navigation_customize -> {
                    setupCustomBottomNav(1,true)
                    binding.tvTitle.text = getString(R.string.menu_customize)
                    binding.ibBackButton.visibility = View.INVISIBLE
                    binding.ibHowToUseButton.visibility = View.INVISIBLE
                    binding.ifvPro.visibility = View.INVISIBLE
                    binding.ifvInfoRight.visibility = View.INVISIBLE
                    "customize_screen"
                }
                R.id.navigation_island -> {
                    setupCustomBottomNav(2,true)
                    binding.tvTitle.text = getString(R.string.menu_dynamic_island)
                    binding.ibBackButton.visibility = View.INVISIBLE
                    binding.ibHowToUseButton.visibility = View.INVISIBLE
                    binding.ifvPro.visibility = View.INVISIBLE
                    binding.ifvInfoRight.visibility = View.VISIBLE
                    "island_screen"
                }
                R.id.navigation_view_all_emoji -> {
                    setupCustomBottomNav(0,true)
                    binding.tvTitle.text = getString(R.string.view_all_battery_emoji)
                    binding.ibBackButton.visibility = View.VISIBLE
                    binding.ibHowToUseButton.visibility = View.INVISIBLE
                    binding.ifvPro.visibility = View.INVISIBLE
                    binding.ifvInfoRight.visibility = View.INVISIBLE
                    "view_all_emoji_screen"
                }
                R.id.navigation_view_all_widget -> {
                    setupCustomBottomNav(0,true)
                    binding.tvTitle.text = getString(R.string.view_all_battery_widget)
                    binding.ibBackButton.visibility = View.VISIBLE
                    binding.ibHowToUseButton.visibility = View.INVISIBLE
                    binding.ifvPro.visibility = View.INVISIBLE
                    binding.ifvInfoRight.visibility = View.INVISIBLE
                    "view_all_widget_screen"
                }
                R.id.navigation_view_all_animation -> {
                    setupCustomBottomNav(0,true)
                    binding.tvTitle.text = getString(R.string.view_all_battery_animation)
                    binding.ibBackButton.visibility = View.VISIBLE
                    binding.ibHowToUseButton.visibility = View.INVISIBLE
                    binding.ifvPro.visibility = View.INVISIBLE
                    binding.ifvInfoRight.visibility = View.INVISIBLE
                    "view_all_animation_screen"
                }
                else -> null
            }

            screenName?.let {
                FirebaseAnalyticsUtils.logClickEvent(requireContext(), "nav_screen_view", mapOf("screen_name" to it))
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            askNotificationPermission()
        }


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
            .setTitle(getString(R.string.enable_notifications))
            .setMessage(getString(R.string.notification_mesg))
            .setPositiveButton(getString(R.string.allow)) { _, _ ->
                markNotificationDialogShown()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton(getString(R.string.later)) { dialogInterface, _ ->
                markNotificationDialogShown()
                dialogInterface.dismiss()
            }
            .setCancelable(false)
            .create()
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.show()
    }

    private fun showRationaleDialog() {
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.why_we_need_notification_access))
            .setMessage(getString(R.string.notification_mesg_2))
            .setPositiveButton(getString(R.string.open_settings)) { _, _ ->
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.cancel), null)
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
        if (preferences.isProUser) {
            binding.bannerAdHome.visibility = View.GONE
            binding.ifvPro.visibility = View.GONE
            return
        }
        if (isBannerHomeEnabled) {
            binding.shimmerBanner.visibility = View.VISIBLE
            BannerAdHelper.loadBannerAd(
                context = requireActivity(),
                container = binding.bannerAdHome,
                bannerAdId = getBannerId(true),
                isCollapsable = true,
                isProUser = preferences.isProUser,
                remoteConfig = isBannerHomeEnabled
            )
        } else if (isNativeHomeEnabled) {
            binding.shimmerBanner.visibility = View.GONE
            nativeHelper = NativeBannerSizeHelper(
                context = requireActivity(),
                adId = getNativeHomeId(), // Replace with your real AdMob ID
                showAdRemoteFlag = isNativeHomeEnabled, // Or get from remote config
                isProUser = preferences.isProUser,       // Or from preferences
                adContainer = binding.bannerAdContainer,
                onAdLoaded = { Log.d("AD", "Banner Ad loaded!") },
                onAdFailed = { Log.d("AD", "Banner Ad failed!") }
            )
        } else {
            binding.bannerAdHome.visibility = View.GONE
        }

    }

    private fun setupCustomBottomNav(index: Int = 0, isForSelection: Boolean = false) {
        val items = listOf(
            NavItem(
                binding.iconHome,
                binding.textHome,
                R.drawable.ic_home_selected,
                R.drawable.ic_home_unselected
            ),
            NavItem(
                binding.iconCustomization,
                binding.textCustomization,
                R.drawable.ic_customization_selected,
                R.drawable.ic_customization_unselected
            ),
            NavItem(
                binding.iconIsland,
                binding.textIsland,
                R.drawable.ic_island_selected,
                R.drawable.ic_island_unselected
            ),
            NavItem(
                binding.iconTemplates,
                binding.textTemplates,
                R.drawable.ic_templates_selected,
                R.drawable.ic_templates_unselected
            )
        )

        val containerIds = listOf(
            binding.menuHome,
            binding.menuCustomization,
            binding.menuIsland,
            binding.menuTemplates
        )

        fun setSelected(index: Int) {
            for (i in items.indices) {
                val item = items[i]
                if (i == index) {
                    item.icon.setImageResource(item.selectedIcon)
                    item.text.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.bottom_nav_selected
                        )
                    )
                } else {
                    item.icon.setImageResource(item.unselectedIcon)
                    item.text.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.bottom_nav_unselected
                        )
                    )
                }
            }
        }

        if (!isForSelection){
            containerIds.forEachIndexed { index, container ->
                container.setOnClickListener {
                    setSelected(index)
                    when (index) {
                        0 -> navigateTo("home")
                        1 -> navigateTo("customization")
                        2 -> navigateTo("island")
                        3 -> navigateTo("templates")
                    }
                }
            }

            // Set initial selection
            setSelected(0)
        }else{
            setSelected(index)
        }
    }

    private fun navigateTo(page: String) {
        when (page) {
            "customization" -> {
                Log.d("navCustom", "Customize Reselected")
                val current = navController.currentDestination?.id
                if (current in listOf(R.id.navigation_view_all_emoji, R.id.navigation_view_all_widget, R.id.navigation_view_all_animation)) {
                    navController.navigateUp()
                    Log.d("navCustom", "Customize Reselected")
                    FirebaseAnalyticsUtils.logClickEvent(requireContext(), "main_to_customize", null)
                    navHostFragment.navController.navigate(R.id.navigation_customize)

                } else {
                    FirebaseAnalyticsUtils.logClickEvent(requireContext(), "main_to_customize", null)
                    Log.d("navCustom", "Customize Reselected r")
                    navHostFragment.navController.navigate(R.id.navigation_customize)
                }
            }

            "home" -> {
                Log.d("navCustom", "Customize Reselected")
                val current = navController.currentDestination?.id
                if (current in listOf(
                        R.id.navigation_view_all_emoji,
                        R.id.navigation_view_all_widget,
                        R.id.navigation_view_all_animation
                    )
                ) {
                    navController.navigateUp()
                    Log.d("navCustom", "Customize Reselected")
                    navHostFragment.navController.navigate(R.id.navigation_home)

                } else {
                    Log.d("navCustom", "Customize Reselected r")
                    navHostFragment.navController.navigate(R.id.navigation_home)

                }
            }
            "island" -> {
                Log.d("navCustom", "Customize Reselected")
                val current = navController.currentDestination?.id
                if (current in listOf(R.id.navigation_view_all_emoji, R.id.navigation_view_all_widget, R.id.navigation_view_all_animation)) {
                    navController.navigateUp()
                    Log.d("navCustom", "Customize Reselected")
                    navHostFragment.navController.navigate(R.id.navigation_island)

                } else {
                    Log.d("navCustom", "Customize Reselected r")
                    navHostFragment.navController.navigate(R.id.navigation_island)

                }
            }
        }
    }

    override fun onResume() {
        if (preferences.isProUser) {
            binding.bannerAdHome.visibility = View.GONE
            binding.ifvPro.visibility = View.GONE
        }
        super.onResume()
    }
}