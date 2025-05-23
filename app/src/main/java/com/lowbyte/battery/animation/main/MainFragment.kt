package com.lowbyte.battery.animation.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.FragmentIntroBinding
import com.lowbyte.battery.animation.databinding.FragmentMainBinding

class MainFragment : Fragment(R.layout.fragment_main) {

    private lateinit var binding: FragmentMainBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentMainBinding.bind(view)

        super.onViewCreated(view, savedInstanceState)
    }

}