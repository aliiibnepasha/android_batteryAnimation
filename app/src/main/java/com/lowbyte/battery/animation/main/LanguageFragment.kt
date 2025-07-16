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
import androidx.core.text.layoutDirection
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.lowbyte.battery.animation.MyApplication
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.activity.SplashActivity
import com.lowbyte.battery.animation.adapter.LanguageAdapter
import com.lowbyte.battery.animation.ads.NativeLanguageHelper
import com.lowbyte.battery.animation.databinding.FragmentLanguageBinding
import com.lowbyte.battery.animation.model.Language
import com.lowbyte.battery.animation.utils.AnimationUtils.finishingLang
import com.lowbyte.battery.animation.utils.AnimationUtils.getNativeLanguageId
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeLangFirstEnabled
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils
import com.lowbyte.battery.animation.utils.LocaleHelper
import java.util.Locale

class LanguageFragment : Fragment(R.layout.fragment_language) {

    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: LanguageAdapter
    private var selectedLanguage: String = "en"
    private lateinit var preferences: AppPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Log.d("backPress","Move Next")
            if (isAdded && findNavController().currentDestination?.id == R.id.languageFragment) {
                (requireActivity() as SplashActivity).changeLanguage(selectedLanguage)
                NativeLanguageHelper.destroy(getNativeLanguageId())
                binding.nativeAdLangFirstContainer.removeAllViews()
                if (LocaleHelper.getLanguage(requireContext()) != "") {
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
            selectedLanguage = language.code
            binding.ibNextButton.visibility = View.VISIBLE

            LocaleHelper.setLocale(requireContext(), language.code)
            val newLocale = Locale(language.code)
            Locale.setDefault(newLocale)
            val layoutDirection =
                if (newLocale.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                    View.LAYOUT_DIRECTION_RTL
                } else {
                    View.LAYOUT_DIRECTION_LTR
                }

            requireActivity().window.decorView.layoutDirection = layoutDirection
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
                    binding.nativeAdLangFirstContainer.removeAllViews()
                    (requireActivity() as SplashActivity).changeLanguage(selectedLanguage)
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

        if ((!requireActivity().isDestroyed && !requireActivity().isFinishing) && isAdded && !finishingLang) {
            Log.d("ADNativeFunc", "Native ad shown call Language")
            finishingLang = true
            Handler(Looper.getMainLooper()).postDelayed({
                if (isAdded && !isDetached) {
                    NativeLanguageHelper.loadAd(
                        context = requireActivity(),
                        adId = getNativeLanguageId(),
                        showAdRemoteFlag = isNativeLangFirstEnabled,
                        isProUser = preferences.isProUser,
                        adContainer = binding.nativeAdLangFirstContainer,
                        onAdLoaded = {
                            Log.d("AD", "Native ad shown")
                        },
                        onAdFailed = {
                            Log.d("AD", "Ad failed to load")
                        }
                    )
                }

            }, 500)


        }

    }


    override fun onDestroyView() {
        NativeLanguageHelper.destroy(getNativeLanguageId())
        _binding = null
        super.onDestroyView()

    }
}