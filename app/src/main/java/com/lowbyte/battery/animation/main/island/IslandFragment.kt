package com.lowbyte.battery.animation.main.island

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lowbyte.battery.animation.databinding.FragmentIslandBinding
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class IslandFragment : Fragment() {

    private var _binding: FragmentIslandBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIslandBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Log screen view event
        FirebaseAnalyticsUtils.logScreenView(this, "IslandFragment")

        val viewModel = ViewModelProvider(this)[IslandViewModel::class.java]

        viewModel.text.observe(viewLifecycleOwner) { value ->
            binding.textNotifications.text = value

            // Optional: Log dynamic content load if useful
            FirebaseAnalyticsUtils.logClickEvent(
                requireActivity(),
                "island_fragment_text_observed",
                mapOf("value" to value)
            )
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}