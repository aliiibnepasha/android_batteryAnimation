package com.lowbyte.battery.animation.main

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.activity.SettingsActivity
import com.lowbyte.battery.animation.databinding.FragmentMainBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.getBannerId

class MainFragment : Fragment(R.layout.fragment_main) {

    private lateinit var binding: FragmentMainBinding
    private var doubleBackPressedOnce = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentMainBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        val navHostFragment = childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)
        loadBannerAd()

        binding.ifvSetting.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        // Handle back press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (navController.currentDestination?.id == R.id.navigation_view_all_emoji ||
                navController.currentDestination?.id == R.id.navigation_view_all_widget ||
                navController.currentDestination?.id == R.id.navigation_view_all_animation
            ) {
                navController.navigateUp()
            } else {
                if (doubleBackPressedOnce) {
                    requireActivity().finish() // Exit the app
                } else {
                    doubleBackPressedOnce = true
                    Toast.makeText(requireContext(),
                        getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        doubleBackPressedOnce = false
                    }, 2000) // 2-second window
                }
            }
        }

        binding.ibBackButton.setOnClickListener {
            navController.navigateUp()
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_home -> {
                    binding.tvTitle.text = getString(R.string.title_home)
                    binding.ibBackButton.visibility = View.INVISIBLE
                }
                R.id.navigation_customize -> {
                    binding.tvTitle.text = getString(R.string.menu_customize)
                    binding.ibBackButton.visibility = View.INVISIBLE
                }
                R.id.navigation_island -> {
                    binding.tvTitle.text = getString(R.string.menu_dynamic_island)
                    binding.ibBackButton.visibility = View.INVISIBLE
                }
                R.id.navigation_view_all_emoji -> {
                    binding.tvTitle.text = getString(R.string.view_all_battery_emoji)
                    binding.ibBackButton.visibility = View.VISIBLE
                }
                R.id.navigation_view_all_widget -> {
                    binding.tvTitle.text = getString(R.string.view_all_battery_widget)
                    binding.ibBackButton.visibility = View.VISIBLE
                }
                R.id.navigation_view_all_animation -> {
                    binding.tvTitle.text = getString(R.string.view_all_battery_animation)
                    binding.ibBackButton.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun loadBannerAd() {
        binding.bannerAdHome.visibility = View.VISIBLE
        val adWidthPixels = Resources.getSystem().displayMetrics.widthPixels
        val adWidth = (adWidthPixels / Resources.getSystem().displayMetrics.density).toInt()
        val adSize =
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(requireContext(), adWidth)
        val adView = AdManagerAdView(requireContext()).apply {
            adUnitId = getBannerId()
            setAdSize(adSize) // ✅ Set ad size correctly
        }

        // Add ad listeners
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d("BannerAd", "Ad Loaded")
                binding.bannerAdHome.visibility = View.VISIBLE

            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e("BannerAd", "Ad Failed to Load: ${adError.message}")
                binding.bannerAdHome.visibility = View.GONE

            }

            override fun onAdOpened() {
                Log.d("BannerAd", "Ad Opened")
            }

            override fun onAdClicked() {
                Log.d("BannerAd", "Ad Clicked")
            }

            override fun onAdClosed() {
                Log.d("BannerAd", "Ad Closed")
            }

            override fun onAdImpression() {
                Log.d("BannerAd", "Ad Impression Logged")
            }
        }

        binding.bannerAdHome.removeAllViews()
        binding.bannerAdHome.addView(adView)

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }
}