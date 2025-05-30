package com.lowbyte.battery.animation.main.view_all

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.lowbyte.battery.animation.activity.ApplySuccessfullyActivity
import com.lowbyte.battery.animation.activity.EmojiEditApplyActivity
import com.lowbyte.battery.animation.activity.StatusBarIconSettingsActivity
import com.lowbyte.battery.animation.adapter.AllEmojiAdapter
import com.lowbyte.battery.animation.databinding.ItemViewPagerBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.emojiAnimListFantasy
import com.lowbyte.battery.animation.utils.AnimationUtils.emojiBasicListFantasy
import com.lowbyte.battery.animation.utils.AnimationUtils.emojiComicListFantasy
import com.lowbyte.battery.animation.utils.AnimationUtils.emojiCuteListFantasy
import com.lowbyte.battery.animation.utils.AnimationUtils.emojiFashionListFantasy
import com.lowbyte.battery.animation.utils.AnimationUtils.emojiListFantasy

class ViewPagerEmojiItemFragment : Fragment() {
    private lateinit var binding: ItemViewPagerBinding
    private lateinit var adapter: AllEmojiAdapter
    private var currentPos: Int = 0

    companion object {
        fun newInstance(position: Int) = ViewPagerEmojiItemFragment().apply {
            arguments = Bundle().apply {
                currentPos = position
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ItemViewPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = AllEmojiAdapter { position,label ->
            val intent = Intent(requireActivity(), EmojiEditApplyActivity::class.java)
            intent.putExtra("EXTRA_POSITION", position)
            intent.putExtra("EXTRA_LABEL", label)
            startActivity(intent)


        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@ViewPagerEmojiItemFragment.adapter
        }

        when (currentPos) {
            0 -> {

                adapter.submitList(emojiListFantasy)
            }

            1 -> {

                adapter.submitList(emojiAnimListFantasy)
            }

            2 -> {

                adapter.submitList(emojiBasicListFantasy)
            }

            3 -> {

                adapter.submitList(emojiCuteListFantasy)
            }

            4 -> {

                adapter.submitList(emojiFashionListFantasy)
            }

            5 -> {

                adapter.submitList(emojiListFantasy)
            }
        }

    }
} 