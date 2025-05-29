package com.lowbyte.battery.animation.main.view_all

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.adapter.AnimationAdapter
import com.lowbyte.battery.animation.databinding.FragmentViewAllAnimationBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.animationList
import com.lowbyte.battery.animation.utils.AnimationUtils.animationListNew
import com.lowbyte.battery.animation.utils.AppPreferences

class ViewAllAnimationFragment : Fragment() {
    private lateinit var binding: FragmentViewAllAnimationBinding
    private lateinit var adapter: AnimationAdapter
    private lateinit var preferences: AppPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewAllAnimationBinding.inflate(inflater, container, false)
        preferences = AppPreferences.getInstance(requireContext())


        binding.switchVibrateFeedback.isChecked = preferences.statusLottieName.isNotBlank()

        binding.switchVibrateFeedback.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                preferences.statusLottieName = "a_1"
                requireActivity().sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))
            } else {
                preferences.statusLottieName = ""
                requireActivity().sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))

            }
        }

        binding.enableSizeAnim.text = getString(R.string.height_dp, preferences.getIconSize("lottieView", 24))

        binding.seekBarAnimSize.progress = preferences.getIconSize("lottieView", 24)

        binding.seekBarAnimSize.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: android.widget.SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                preferences.setIconSize("lottieView",progress)
                binding.enableSizeAnim.text = getString(R.string.height_dp, preferences.getIconSize("lottieView", 24))
                requireActivity().sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })



        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = AnimationAdapter { position, name ->
            Log.d("Click", "Clicked position: $position, animation: $name")
            preferences.statusLottieName = name
            requireActivity().sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))

        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@ViewAllAnimationFragment.adapter
        }
        adapter.submitList(animationList)
    }
}