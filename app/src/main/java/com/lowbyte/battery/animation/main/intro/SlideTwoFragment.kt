package com.lowbyte.battery.animation.main.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lowbyte.battery.animation.databinding.FragmentSlideOneBinding
import com.lowbyte.battery.animation.databinding.FragmentSlideTwoBinding

class SlideTwoFragment : Fragment() {
    private var _binding: FragmentSlideTwoBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_TEXT = "text"

        fun newInstance(text: String) = SlideTwoFragment().apply {
            arguments = Bundle().apply { putString(ARG_TEXT, text) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSlideTwoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
      
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}