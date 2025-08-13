package com.lowbyte.battery.animation.main.view_all

import FileItem
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.lowbyte.battery.animation.activity.EmojiEditApplyActivity
import com.lowbyte.battery.animation.activity.ProActivity
import com.lowbyte.battery.animation.adapter.AllEmojiAdapter
import com.lowbyte.battery.animation.ads.RewardedAdManager
import com.lowbyte.battery.animation.databinding.ItemViewPagerBinding
import com.lowbyte.battery.animation.server.EmojiViewModel
import com.lowbyte.battery.animation.server.EmojiViewModelFactory
import com.lowbyte.battery.animation.server.Resource
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_LABEL
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_POSITION
import com.lowbyte.battery.animation.utils.AnimationUtils.dataUrl
import com.lowbyte.battery.animation.utils.AnimationUtils.dp
import com.lowbyte.battery.animation.utils.AnimationUtils.isRewardedEnabled
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils
import com.lowbyte.battery.animation.utils.RewardedDialogUtils
import kotlinx.coroutines.launch

class ViewPagerEmojiItemFragment : Fragment() {

    private lateinit var binding: ItemViewPagerBinding
    private lateinit var adapter: AllEmojiAdapter
    private var currentPos: Int = 0
    private lateinit var glm: GridLayoutManager
    private var categoryTitle: String = ""
    private lateinit var rewardedDialog: RewardedDialogUtils
    private lateinit var preferences: AppPreferences


    private val vm: EmojiViewModel by viewModels { EmojiViewModelFactory(requireContext()) }

    companion object {
        private const val ARG_POSITION = "arg_position"

        fun newInstance(position: Int, category: String) = ViewPagerEmojiItemFragment().apply {
            arguments = Bundle().apply {
                putString("category", category)
                putInt(ARG_POSITION, position)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentPos = arguments?.getInt(ARG_POSITION) ?: 0
        categoryTitle = arguments?.getString("category") ?: "0"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ItemViewPagerBinding.inflate(inflater, container, false)
        preferences = AppPreferences.getInstance(requireContext())
        FirebaseAnalyticsUtils.logScreenView(this, "EmojiTab_$currentPos")
        rewardedDialog = RewardedDialogUtils(
            activity = requireActivity(),
            isProUser = preferences.isProUser,
            isRewardedEnabled = isRewardedEnabled
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch {
                    vm.pngs.collect {
                        renderPngs(it)
                    }
                }
            }
        }
        vm.loadCategory(dataUrl, name = categoryTitle)
        vm.loadFolderPngs(dataUrl, categoryName = categoryTitle, folderName = "emoji_preview")
        setupRecyclerView()
    }


    private fun renderPngs(state: Resource<List<FileItem>>) {
        when (state) {
            is Resource.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
            }

            is Resource.Error -> {
                binding.progressBar.visibility = View.GONE
            }

            is Resource.Success -> {
                binding.progressBar.visibility = View.GONE
                Log.d("EmojiTab", "Tab $currentPos loaded with ${state.data.size} items.")
                val firstVisible = glm.findFirstVisibleItemPosition()
                val offset = binding.recyclerView.getChildAt(0)?.top ?: 0
                adapter.submitList(state.data) {
                    glm.scrollToPositionWithOffset(firstVisible, offset)
                }
            }

        }


    }

    private fun setupRecyclerView() {
        val spaceHPx = 40.dp(requireContext()) // top & bottom spacer height
        val spacePPx = 60.dp(requireContext()) // top & bottom spacer height

        adapter = AllEmojiAdapter (
            { position, fileItem, isRewarded ->
            FirebaseAnalyticsUtils.logClickEvent(
                requireActivity(),
                "emoji_selected",
                mapOf(
                    "tab_index" to currentPos.toString(),
                    "emoji_label" to fileItem.name,
                    "emoji_position" to position.toString()
                )
            )
//            if (isRewarded && !preferences.isProUser) {
//
//                rewardedDialog.init(
//                    onWatchAd = {
//                        RewardedAdManager.showRewardedAd(
//                            activity = requireActivity(),
//                            onRewardEarned = { /* optional */ },
//                            onAdShown = { /* optional */ },
//                            onAdDismissed = {
//                                val intent = Intent(
//                                    requireActivity(), EmojiEditApplyActivity::class.java
//                                ).apply {
//                                    putExtra(EXTRA_POSITION, position)
//                                    putExtra(EXTRA_LABEL, fileItem.name)
//                                }
//                                startActivity(intent)
//                                FirebaseAnalyticsUtils.logClickEvent(
//                                    requireActivity(),
//                                    "EmojiEditApplyAct"
//                                )
//                            }
//                        )
//                    },
//                    onGoPremium = {
//                        startActivity(Intent(requireActivity(), ProActivity::class.java))
//                        FirebaseAnalyticsUtils.logClickEvent(
//                            requireActivity(),
//                            "ProActivity"
//                        )
//                    }
//                )
//                rewardedDialog.show()
//            } else {
                val intent = Intent(requireActivity(), EmojiEditApplyActivity::class.java).apply {
                    putExtra(EXTRA_POSITION, position)
                    putExtra(EXTRA_LABEL, fileItem.name)
                }
                startActivity(intent)
                FirebaseAnalyticsUtils.logClickEvent(
                    requireActivity(),
                    "EmojiEditApplyAct"
                )
         //   }
        },
            headerHeightPx = spaceHPx,
            footerHeightPx = spacePPx,
            categoryName = categoryTitle,
            folderName = "emoji_preview"
        )
        glm = GridLayoutManager(requireContext(), 3).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == 0 || position == adapter.itemCount - 1) {
                        3
                    } else {
                        1
                    }
                }
            }
        }

        binding.recyclerView.apply {
            layoutManager = glm
            adapter = this@ViewPagerEmojiItemFragment.adapter
        }

    }

    override fun onDestroyView() {
        rewardedDialog.dismiss()
        super.onDestroyView()
    }
}