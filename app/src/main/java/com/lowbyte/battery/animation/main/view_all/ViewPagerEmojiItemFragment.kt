package com.lowbyte.battery.animation.main.view_all

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.lowbyte.battery.animation.activity.EmojiEditApplyActivity
import com.lowbyte.battery.animation.adapter.AllEmojiAdapter
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.databinding.ItemViewPagerBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_LABEL
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_POSITION
import com.lowbyte.battery.animation.utils.AnimationUtils.cute
import com.lowbyte.battery.animation.utils.AnimationUtils.emojiAnimListFantasy
import com.lowbyte.battery.animation.utils.AnimationUtils.emojiBasicListFantasy
import com.lowbyte.battery.animation.utils.AnimationUtils.emojiComicListFantasy
import com.lowbyte.battery.animation.utils.AnimationUtils.emojiCuteListFantasy
import com.lowbyte.battery.animation.utils.AnimationUtils.emojiFace
import com.lowbyte.battery.animation.utils.AnimationUtils.emojiFashionListFantasy
import com.lowbyte.battery.animation.utils.AnimationUtils.emojiListCartoon
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenHomeEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.pet
import com.lowbyte.battery.animation.utils.AnimationUtils.toy
import com.lowbyte.battery.animation.utils.AnimationUtils.trendy
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class ViewPagerEmojiItemFragment : Fragment() {

    private lateinit var binding: ItemViewPagerBinding
    private lateinit var adapter: AllEmojiAdapter
    private var currentPos: Int = 0

    private lateinit var preferences: AppPreferences

    companion object {
        private const val ARG_POSITION = "arg_position"

        fun newInstance(position: Int) = ViewPagerEmojiItemFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_POSITION, position)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentPos = arguments?.getInt(ARG_POSITION) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ItemViewPagerBinding.inflate(inflater, container, false)
        preferences = AppPreferences.getInstance(requireContext())

        // Log screen view for emoji tab
        FirebaseAnalyticsUtils.logScreenView(this, "EmojiTab_$currentPos")



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
       // RewardedAdManager.loadAd(requireActivity())
    }

    private fun setupRecyclerView() {
        adapter = AllEmojiAdapter { position, label, isRewarded ->
            FirebaseAnalyticsUtils.logClickEvent(
                requireActivity(),
                "emoji_selected",
                mapOf(
                    "tab_index" to currentPos.toString(),
                    "emoji_label" to label,
                    "emoji_position" to position.toString()
                )
            )

            AdManager.showInterstitialAd(
                requireActivity(),
                isFullscreenHomeEnabled,
                true
            ) {
                AdManager.setCooldownEnabledForShow(true)
                AdManager.setCooldownEnabledForLoad(true)
                val intent = Intent(requireActivity(), EmojiEditApplyActivity::class.java).apply {
                    putExtra(EXTRA_POSITION, position)
                    putExtra(EXTRA_LABEL, label)
                    putExtra("RewardEarned", isRewarded)
                }
                startActivity(intent)
                FirebaseAnalyticsUtils.logClickEvent(
                    requireActivity(),
                    "EmojiEditApplyAct"
                )
            }


        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@ViewPagerEmojiItemFragment.adapter
        }

        val emojiList = when (currentPos) {
            0 -> toy
            1 -> emojiFace
            2 -> trendy
            3 -> pet
            4 -> cute

            5 -> emojiFashionListFantasy
            6 -> emojiAnimListFantasy
            7 -> emojiBasicListFantasy
            8 -> emojiCuteListFantasy
            9 -> emojiListCartoon
            10 -> emojiComicListFantasy
            else -> emptyList()
        }

        Log.d("EmojiTab", "Tab $currentPos loaded with ${emojiList.size} items.")
        adapter.submitList(emojiList)
    }

    override fun onDestroyView() {

        super.onDestroyView()
    }
}