package com.lowbyte.battery.animation.main.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.activity.BatteryAnimationEditApplyActivity
import com.lowbyte.battery.animation.activity.BatteryWidgetEditApplyActivity
import com.lowbyte.battery.animation.activity.EmojiEditApplyActivity
import com.lowbyte.battery.animation.adapter.MultiViewAdapter
import com.lowbyte.battery.animation.databinding.FragmentHomeBinding
import com.lowbyte.battery.animation.model.MultiViewItem
import com.lowbyte.battery.animation.utils.AnimationUtils.emojiCuteListFantasy
import com.lowbyte.battery.animation.utils.AnimationUtils.widgetListAction
import com.lowbyte.battery.animation.utils.AnimationUtils.widgetListFantasy

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentHomeBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        val data = listOf(
            MultiViewItem.TitleItem(getString(R.string.cat_emojis)),
            MultiViewItem.ListItem(emojiCuteListFantasy),
            MultiViewItem.TitleItem(getString(R.string.cat_widgets)),
            MultiViewItem.ListItem(widgetListAction),
            MultiViewItem.TitleItem(getString(R.string.cat_animations)),
            MultiViewItem.ListItem(widgetListFantasy),
        )

        val adapter = MultiViewAdapter(
            data,
            onChildItemClick = { parentPosition, name, parentPos ->
                when (parentPos) {
                    1 -> {
                        val intent = Intent(requireActivity(), EmojiEditApplyActivity::class.java)
                        intent.putExtra("EXTRA_POSITION", parentPosition)
                        intent.putExtra("EXTRA_LABEL", name)
                        startActivity(intent)
                    }
                    3 -> {
                        val intent = Intent(requireActivity(), BatteryWidgetEditApplyActivity::class.java)
                        intent.putExtra("EXTRA_POSITION", parentPosition)
                        intent.putExtra("EXTRA_LABEL", name)
                        startActivity(intent)
                    }
                    5 -> {
                        val intent = Intent(requireActivity(), BatteryAnimationEditApplyActivity::class.java)
                        intent.putExtra("EXTRA_POSITION", parentPosition)
                        intent.putExtra("EXTRA_LABEL", name)
                        startActivity(intent)
                    }
                }

            },
            onChildViewAllClick = { titlePosition ->
                when (titlePosition) {
                    0 -> {
                        findNavController().navigate(R.id.action_home_to_viewAllEmoji)
                    }

                    2 -> {
                        findNavController().navigate(R.id.action_home_to_viewAllWidget)

                    }

                    4 -> {
                        findNavController().navigate(R.id.action_home_to_viewAllAnim)

                    }
                }
            }
        )

        binding.recyclerViewParent.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewParent.adapter = adapter

    }
}