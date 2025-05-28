package com.lowbyte.battery.animation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.lowbyte.battery.animation.adapter.LanguageAdapter
import com.lowbyte.battery.animation.databinding.FragmentLanguageBinding

class LanguageFragment : Fragment(R.layout.fragment_language) {

    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: LanguageAdapter
    private var selectedLanguage: String = "English"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentLanguageBinding.bind(view)

        val languages = listOf("English", "Spanish", "French", "Hindi", "Arabic")

        adapter = LanguageAdapter(languages) { language ->
            selectedLanguage = language
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.ibBackButton.setOnClickListener {
            requireActivity().finish()
        }

        binding.ibNextButton.setOnClickListener {
            findNavController().navigate(R.id.action_language_to_intro)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}