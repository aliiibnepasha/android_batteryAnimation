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
import com.lowbyte.battery.animation.activity.EmojiEditApplyActivity
import com.lowbyte.battery.animation.adapter.AllEmojiAdapter
import com.lowbyte.battery.animation.ads.RewardedAdManager
import com.lowbyte.battery.animation.databinding.DialogGoProBinding
import com.lowbyte.battery.animation.databinding.ItemViewPagerBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_LABEL
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_POSITION
import com.lowbyte.battery.animation.utils.AnimationUtils.emojiAnimListFantasy
import com.lowbyte.battery.animation.utils.AnimationUtils.emojiBasicListFantasy
import com.lowbyte.battery.animation.utils.AnimationUtils.emojiComicListFantasy
import com.lowbyte.battery.animation.utils.AnimationUtils.emojiCuteListFantasy
import com.lowbyte.battery.animation.utils.AnimationUtils.emojiFashionListFantasy
import com.lowbyte.battery.animation.utils.AnimationUtils.emojiListCartoon
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class ViewPagerEmojiItemFragment : Fragment() {

    private lateinit var binding: ItemViewPagerBinding
    private lateinit var adapter: AllEmojiAdapter
    private var currentPos: Int = 0

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

        // Log screen view for emoji tab
        FirebaseAnalyticsUtils.logScreenView(this, "EmojiTab_$currentPos")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        RewardedAdManager.loadAd(requireActivity())
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
            if (isRewarded) {
                val dialog = Dialog(requireContext())
                val binding = DialogGoProBinding.inflate(LayoutInflater.from(requireContext()))
                dialog.setContentView(binding.root)
                dialog.setCancelable(true)

                dialog.window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

                binding.ivClose.setOnClickListener { dialog.dismiss() }

                binding.btnWatchAd.setOnClickListener {
                    dialog.dismiss()
                    RewardedAdManager.showRewardedAd(
                        activity = requireActivity(),
                        onRewardEarned = {
                            val intent = Intent(
                                requireActivity(), EmojiEditApplyActivity::class.java
                            ).apply {
                                putExtra(EXTRA_POSITION, position)
                                putExtra(EXTRA_LABEL, label)
                            }
                            startActivity(intent)
                        },
                        onAdShown = {
                            // Log analytics or UI update
                        },
                        onAdDismissed = {
                            Toast.makeText(requireActivity(), "Ad dismissed", Toast.LENGTH_SHORT)
                                .show()
                        })
                }

                binding.btnPremium.setOnClickListener {
                    dialog.dismiss()
                    // handle premium click (maybe navigate to paywall)
                }

                dialog.show()
            } else {
                val intent = Intent(requireActivity(), EmojiEditApplyActivity::class.java).apply {
                    putExtra(EXTRA_POSITION, position)
                    putExtra(EXTRA_LABEL, label)
                }
                startActivity(intent)
            }
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@ViewPagerEmojiItemFragment.adapter
        }

        val emojiList = when (currentPos) {
            0 -> emojiFashionListFantasy
            1 -> emojiAnimListFantasy
            2 -> emojiBasicListFantasy
            3 -> emojiCuteListFantasy
            4 -> emojiListCartoon
            5 -> emojiComicListFantasy
            else -> emptyList()
        }

        Log.d("EmojiTab", "Tab $currentPos loaded with ${emojiList.size} items.")
        adapter.submitList(emojiList)
    }
}