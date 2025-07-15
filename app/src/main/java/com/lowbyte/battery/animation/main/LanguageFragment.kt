package com.lowbyte.battery.animation.main

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.lowbyte.battery.animation.MyApplication
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.activity.SplashActivity
import com.lowbyte.battery.animation.adapter.LanguageAdapter
import com.lowbyte.battery.animation.ads.NativeBannerSizeHelper
import com.lowbyte.battery.animation.ads.NativeLanguageHelper
import com.lowbyte.battery.animation.databinding.FragmentLanguageBinding
import com.lowbyte.battery.animation.model.Language
import com.lowbyte.battery.animation.utils.AnimationUtils.finishingLang
import com.lowbyte.battery.animation.utils.AnimationUtils.getNativeLanguageId
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeLangFirstEnabled
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils
import com.lowbyte.battery.animation.utils.LocaleHelper

class LanguageFragment : Fragment(R.layout.fragment_language) {

    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!
    private var nativeHelper: NativeLanguageHelper? = null

    private lateinit var adapter: LanguageAdapter
    private var selectedLanguage: String = "English"
    private lateinit var preferences: AppPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Log.d("backPress","Move Next")
            if (isAdded && findNavController().currentDestination?.id == R.id.languageFragment) {
                if (LocaleHelper.getLanguage(requireContext()) != "") {
                    requireActivity().recreate()
                    if (preferences.isFirstRun) {
                        findNavController().navigate(R.id.action_language_to_intro)
                    } else {
                        findNavController().navigate(R.id.action_language_to_main)
                    }

                } else {
                    Toast.makeText(requireContext(), "Please select a language", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLanguageBinding.bind(view)
        preferences = AppPreferences.getInstance(requireContext())
        MyApplication.enableOpenAd(false)

        // Log screen view
        FirebaseAnalyticsUtils.logScreenView(this, "language_screen")

        val languages = listOf(
            Language("العربية", "ar"),
            Language("Español", "es-rES"),
            Language("English", "en"),
            Language("Français", "fr-rFR"),
            Language("हिंदी", "hi"),
            Language("Italiano", "it-rIT"),
            Language("日本語", "ja"),
            Language("한국어", "ko"),
            Language("Bahasa Melayu", "ms-rMY"),
            Language("Filipino", "phi"),
            Language("ไทย", "th"),
            Language("Türkçe", "tr-rTR"),
            Language("Tiếng Việt", "vi"),
            Language("Português", "pt-rPT"),
            Language("Bahasa Indonesia", "in")
        )

        val currentLanguageCode = LocaleHelper.getLanguage(requireContext())
        if (currentLanguageCode == "") {
            binding.ibNextButton.visibility = View.GONE
        } else {
            binding.ibNextButton.visibility = View.VISIBLE
        }

        adapter = LanguageAdapter(languages, currentLanguageCode) { language ->
            selectedLanguage = language.name
            binding.ibNextButton.visibility = View.VISIBLE
            val context =  LocaleHelper.setLocale(requireContext(), language.code)
            resources.updateConfiguration(context.resources.configuration, context.resources.displayMetrics)

            // Log language selection event
            FirebaseAnalyticsUtils.logClickEvent(
                requireActivity(),
                "language_selected",
                mapOf("language_name" to language.name, "language_code" to language.code)
            )

        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.ibBackButton.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(requireActivity(), "click_language_back")
            requireActivity().finish()
        }

        binding.ibNextButton.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(requireActivity(), "click_language_next")
            if (isAdded && findNavController().currentDestination?.id == R.id.languageFragment) {
                if (LocaleHelper.getLanguage(requireContext()) != "") {
                    requireActivity().recreate()
                        if (preferences.isFirstRun) {
                            findNavController().navigate(R.id.action_language_to_intro)
                        } else {
                            findNavController().navigate(R.id.action_language_to_main)
                        }
                } else {
                    Toast.makeText(requireContext(), "Please select a language", Toast.LENGTH_SHORT).show()
                }
            }
        }

        if ((!requireActivity().isDestroyed && !requireActivity().isFinishing) && !finishingLang) {
            finishingLang  = true
            nativeHelper = NativeLanguageHelper(
                context = requireActivity(),
                adId = getNativeLanguageId(),
                showAdRemoteFlag = true,
                isProUser = preferences.isProUser,
                adContainer = binding.nativeAdLangFirstContainer,
                onAdLoaded = {
                    Log.d("AD", "Native ad shown") },
                onAdFailed = {
                    Log.d("AD", "Ad failed to load") }
            )
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        nativeHelper?.destroy()
        nativeHelper = null
        _binding = null
    }
}