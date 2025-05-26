package com.lowbyte.battery.animation.main.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.adapter.MultiViewAdapter
import com.lowbyte.battery.animation.databinding.FragmentHomeBinding
import com.lowbyte.battery.animation.model.MultiViewItem

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentHomeBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        val data = listOf(
            MultiViewItem.TitleItem(getString(R.string.cat_emojis)),
            MultiViewItem.ListItem(listOf(R.drawable.emoji, R.drawable.emoji, R.drawable.emoji)),
            MultiViewItem.TitleItem(getString(R.string.cat_widgets)),
            MultiViewItem.ListItem(listOf(R.drawable.emoji, R.drawable.emoji, R.drawable.emoji)),
            MultiViewItem.TitleItem(getString(R.string.cat_animations)),
            MultiViewItem.ListItem(listOf(R.drawable.emoji, R.drawable.emoji, R.drawable.emoji)),
        )

        val adapter = MultiViewAdapter(
            data,
            onChildItemClick = { parentPosition ->

                Toast.makeText(
                    context,
                    "Child clicked in section $parentPosition",
                    Toast.LENGTH_SHORT
                ).show()
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