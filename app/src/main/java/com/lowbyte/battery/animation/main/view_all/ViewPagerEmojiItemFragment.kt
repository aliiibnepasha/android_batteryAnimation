package com.lowbyte.battery.animation.main.view_all

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.activity.EmojiEditApplyActivity
import com.lowbyte.battery.animation.activity.ProActivity
import com.lowbyte.battery.animation.adapter.AllEmojiAdapter
import com.lowbyte.battery.animation.ads.RewardedAdManager
import com.lowbyte.battery.animation.databinding.DialogGoProBinding
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
import com.lowbyte.battery.animation.utils.AnimationUtils.isRewardedEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.pet
import com.lowbyte.battery.animation.utils.AnimationUtils.toy
import com.lowbyte.battery.animation.utils.AnimationUtils.trendy
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class ViewPagerEmojiItemFragment : Fragment() {

    private lateinit var binding: ItemViewPagerBinding
    private lateinit var adapter: AllEmojiAdapter
    private var currentPos: Int = 0
    private lateinit var dialogRewarded: Dialog

    private lateinit var preferences: AppPreferences

    private var bindingReward: DialogGoProBinding? = null
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

        dialogRewarded = Dialog(requireContext())
        bindingReward = DialogGoProBinding.inflate(LayoutInflater.from(requireContext()))
        dialogRewarded.setContentView(bindingReward?.root!!)
        dialogRewarded.setCancelable(false)
        dialogRewarded.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialogRewarded.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialogRewarded.window?.setBackgroundDrawableResource(android.R.color.transparent)


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
            if (isRewarded && !preferences.isProUser) {

                bindingReward?.ivClose?.setOnClickListener { dialogRewarded.dismiss() }
                if (!isRewardedEnabled){
                    bindingReward?.btnWatchAd?.visibility = View.GONE
                    bindingReward?.btnPremium?.visibility = View.VISIBLE
                }else{
                    bindingReward?.btnWatchAd?.visibility = View.VISIBLE
                    bindingReward?.btnPremium?.visibility = View.VISIBLE
                    bindingReward?.btnWatchAd?.setOnClickListener {
                        dialogRewarded.dismiss()
                        RewardedAdManager.showRewardedAd(
                            activity = requireActivity(),
                            onRewardEarned = {

                            },
                            onAdShown = {
                                // Log analytics or UI update
                            },
                            onAdDismissed = {
                                val intent = Intent(
                                    requireActivity(), EmojiEditApplyActivity::class.java
                                ).apply {
                                    putExtra(EXTRA_POSITION, position)
                                    putExtra(EXTRA_LABEL, label)
                                }
                                startActivity(intent)
                                FirebaseAnalyticsUtils.logClickEvent(
                                    requireActivity(),
                                    "EmojiEditApplyAct"
                                )
                            })
                    }
                }
                bindingReward?.btnPremium?.setOnClickListener {
                    dialogRewarded.dismiss()
                    startActivity(Intent(requireActivity(), ProActivity::class.java))
                    FirebaseAnalyticsUtils.logClickEvent(
                        requireActivity(),
                        "ProActivity"
                    )
                }
                dialogRewarded.show()
            } else {
                val intent = Intent(requireActivity(), EmojiEditApplyActivity::class.java).apply {
                    putExtra(EXTRA_POSITION, position)
                    putExtra(EXTRA_LABEL, label)
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
            0 -> trendy
            1 -> toy
            2 -> emojiFace
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
        if (dialogRewarded.isShowing){
            dialogRewarded.dismiss()
        }
        super.onDestroyView()
    }
}