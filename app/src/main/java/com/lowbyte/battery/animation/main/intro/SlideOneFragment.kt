package com.lowbyte.battery.animation.main.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lowbyte.battery.animation.databinding.FragmentSlideOneBinding

class SlideOneFragment : Fragment() {
    private var _binding: FragmentSlideOneBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_TEXT = "text"

        fun newInstance(text: String) = SlideOneFragment().apply {
            arguments = Bundle().apply { putString(ARG_TEXT, text) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSlideOneBinding.inflate(inflater, container, false)
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