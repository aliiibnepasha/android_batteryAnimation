package com.lowbyte.battery.animation.main.view_all

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_LABEL
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_POSITION
import com.lowbyte.battery.animation.utils.AnimationUtils.allEmojis
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
            intent.putExtra(EXTRA_POSITION, position)
            intent.putExtra(EXTRA_LABEL, label)
            startActivity(intent)


        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@ViewPagerEmojiItemFragment.adapter
        }

        when (currentPos) {
            0 -> {

                adapter.submitList(allEmojis)
                Log.d("TAG", "setupRecyclerView:0  ${allEmojis}")
            }

            1 -> {

                adapter.submitList(emojiListFantasy)
                Log.d("TAG", "setupRecyclerView:1 ${emojiListFantasy}")

            }

            2 -> {

                adapter.submitList(emojiAnimListFantasy)
                Log.d("TAG", "setupRecyclerView:2 ${emojiAnimListFantasy}")

            }

            3 -> {

                adapter.submitList(emojiBasicListFantasy)
                Log.d("TAG", "setupRecyclerView:3 ${emojiBasicListFantasy}")

            }

            4 -> {

                adapter.submitList(emojiCuteListFantasy)
                Log.d("TAG", "setupRecyclerView:4 ${emojiCuteListFantasy}")

            }

            5 -> {

                adapter.submitList(emojiFashionListFantasy)
                Log.d("TAG", "setupRecyclerView:5 ${emojiFashionListFantasy}")

            }
            6 -> {

                adapter.submitList(emojiComicListFantasy)
                Log.d("TAG", "setupRecyclerView:5 ${emojiComicListFantasy}")

            }
        }

    }
} 