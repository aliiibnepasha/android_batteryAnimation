package com.lowbyte.battery.animation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.lowbyte.battery.animation.databinding.FragmentGetStartedBinding
import com.lowbyte.battery.animation.databinding.FragmentIntroBinding

class GetStartedFragment : Fragment(R.layout.fragment_get_started)  {
    private lateinit var binding: FragmentGetStartedBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentGetStartedBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)


        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.action_getStarted_to_main)
        }
    }


}