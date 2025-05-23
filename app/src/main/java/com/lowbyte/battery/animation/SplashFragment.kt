package com.lowbyte.battery.animation

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.lowbyte.battery.animation.databinding.FragmentSplashBinding

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)

        val progressBar = binding.progressBar
        progressBar.max = 100

        val animator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100)
        animator.duration = 5000 // Adjust duration as needed
        animator.start()

        Handler(Looper.getMainLooper()).postDelayed({
            // Navigate to LanguageFragment
            findNavController().navigate(R.id.action_splash_to_language)
        }, animator.duration)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}