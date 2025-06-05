package com.lowbyte.battery.animation

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.lowbyte.battery.animation.adapter.LanguageAdapter
import com.lowbyte.battery.animation.databinding.FragmentLanguageBinding
import com.lowbyte.battery.animation.utils.LocaleHelper
import java.util.Locale

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
            Language("العربية", "ar"),           // Arabic ok
            Language("Español", "es-rES"),       // Spanish (Spain) ok
            Language("Français", "fr-rFR"),      // French (France) ok
            Language("हिंदी", "hi"),             // Hindi ok
            Language("Italiano", "it-rIT"),      // Italian ok
           // Language("עברית", "iw"),             // Hebrew ok
            Language("日本語", "ja"),             // Japanese ok
            Language("한국어", "ko"),             // Korean ok
            Language("Bahasa Melayu", "ms-rMY"), // Malay (Malaysia) ok
            Language("Filipino", "phi"),         // Filipino  ok
            Language("ไทย", "th"),               // Thai ok
            Language("Türkçe", "tr-rTR"),        // Turkish (Turkey) ok
            Language("Tiếng Việt", "vi"),         // Vietnamese ok
            Language("Português", "pt-rPT"),     // Portuguese (Portugal) ok
            Language("Bahasa Indonesia", "in")  // Indonesian ok
        )

        adapter = LanguageAdapter(languages) { language ->
            selectedLanguage = language.name
            // Save selected language
            LocaleHelper.setLocale(requireContext(), language.code)
            // Restart activity to apply changes
            requireActivity().apply {
                recreate()
               // findNavController().navigate(R.id.action_language_to_intro)
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

    data class Language(
        val name: String,
        val code: String
    )
}