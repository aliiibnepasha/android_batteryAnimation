package com.lowbyte.battery.animation.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.activity.addCallback
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.FragmentMainBinding

class MainFragment : Fragment(R.layout.fragment_main) {

    private lateinit var binding: FragmentMainBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentMainBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        val navHostFragment = childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)
        // Handle back press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (navController.currentDestination?.id == R.id.action_home_to_viewAllEmoji) {
                navController.navigateUp()
            } else {
                //requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }


        binding.ibBackButton.setOnClickListener {
            navController.navigateUp()
        }

        // Optional: Add navigation listener
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_home -> {
                    binding.tvTitle.text  = getString(R.string.title_home)
                    binding.ibBackButton.visibility = View.INVISIBLE
                }
                R.id.navigation_customize -> {
                    binding.tvTitle.text  = getString(R.string.menu_customize)
                    binding.ibBackButton.visibility = View.INVISIBLE
                }
                R.id.navigation_island -> {
                    binding.tvTitle.text  = getString(R.string.menu_dynamic_island)
                    binding.ibBackButton.visibility = View.INVISIBLE
                }


                R.id.navigation_view_all_emoji -> {
                    binding.tvTitle.text  = getString(R.string.view_all_battery_emoji)
                    binding.ibBackButton.visibility = View.VISIBLE
                }
                R.id.navigation_view_all_widget -> {
                    binding.tvTitle.text  = getString(R.string.view_all_battery_widget)
                    binding.ibBackButton.visibility = View.VISIBLE
                }
                R.id.navigation_view_all_animation -> {
                    binding.tvTitle.text  = getString(R.string.view_all_battery_animation)
                    binding.ibBackButton.visibility = View.VISIBLE
                }
            }
        }
    }

}