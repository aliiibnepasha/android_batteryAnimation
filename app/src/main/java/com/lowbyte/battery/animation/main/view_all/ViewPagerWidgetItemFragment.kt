package com.lowbyte.battery.animation.main.view_all

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.lowbyte.battery.animation.adapter.AllWidgetAdapter
import com.lowbyte.battery.animation.databinding.ItemViewPagerBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.widgetListAction
import com.lowbyte.battery.animation.utils.AnimationUtils.widgetListBasic
import com.lowbyte.battery.animation.utils.AnimationUtils.widgetListCute
import com.lowbyte.battery.animation.utils.AnimationUtils.widgetListFantasy
import com.lowbyte.battery.animation.utils.AnimationUtils.widgetListW

class ViewPagerWidgetItemFragment : Fragment() {
    private lateinit var binding: ItemViewPagerBinding
    private lateinit var adapter: AllWidgetAdapter
    private var currentPos: Int = 0

    companion object {
        fun newInstance(position: Int) = ViewPagerWidgetItemFragment().apply {
            arguments = Bundle().apply {
                putInt("position", position)
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
        adapter = AllWidgetAdapter { position ->
            // Handle item click
            // Navigate to detail screen or perform action
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@ViewPagerWidgetItemFragment.adapter
        }

        when (currentPos) {
            0 -> {

                adapter.submitList(widgetListW)
                Log.d("listDrawable","$widgetListW")
            }

            1 -> {

                adapter.submitList(widgetListAction)
            }

            2 -> {

                adapter.submitList(widgetListBasic)
            }

            3 -> {

                adapter.submitList(widgetListCute)
            }

            4 -> {

                adapter.submitList(widgetListFantasy)
            }


        }
    }
} 