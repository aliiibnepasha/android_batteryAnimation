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
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }


        binding.ibBackButton.setOnClickListener {
            navController.navigateUp()
        }

        // Optional: Add navigation listener
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_home -> {
                    // Handle home fragment selection
                    binding.ibBackButton.visibility = View.INVISIBLE
                }
                R.id.navigation_customize -> {
                    // Handle customize fragment selection
                    binding.ibBackButton.visibility = View.INVISIBLE
                }
                R.id.navigation_island -> {
                    // Handle island fragment selection
                    binding.ibBackButton.visibility = View.INVISIBLE
                }else->{
                binding.ibBackButton.visibility = View.VISIBLE
                }
            }
        }
    }

}