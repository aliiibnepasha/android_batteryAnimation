package com.lowbyte.battery.animation

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.lowbyte.battery.animation.adapter.LanguageAdapter
import com.lowbyte.battery.animation.databinding.FragmentLanguageBinding
import com.lowbyte.battery.animation.model.Language
import com.lowbyte.battery.animation.utils.LocaleHelper

class LanguageFragment : Fragment(R.layout.fragment_language) {

    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: LanguageAdapter
    private var selectedLanguage: String = "English"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLanguageBinding.bind(view)

        val languages = listOf(
            Language("English", "en"),
            Language("العربية", "ar"),           // Arabic
            Language("Español", "es-rES"),       // Spanish (Spain)
            Language("Français", "fr-rFR"),      // French (France)
            Language("हिंदी", "hi"),             // Hindi
            Language("Italiano", "it-rIT"),      // Italian
            Language("日本語", "ja"),             // Japanese
            Language("한국어", "ko"),             // Korean
            Language("Bahasa Melayu", "ms-rMY"), // Malay (Malaysia)
            Language("Filipino", "phi"),         // Filipino
            Language("ไทย", "th"),               // Thai
            Language("Türkçe", "tr-rTR"),        // Turkish (Turkey)
            Language("Tiếng Việt", "vi"),         // Vietnamese
            Language("Português", "pt-rPT"),     // Portuguese (Portugal)
            Language("Bahasa Indonesia", "in")  // Indonesian
        )

        val currentLanguageCode = LocaleHelper.getLanguage(requireContext())

        adapter = LanguageAdapter(languages, currentLanguageCode) { language ->
            selectedLanguage = language.name
            LocaleHelper.setLocale(requireContext(), language.code)
            requireActivity().apply {
                recreate()
            }
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