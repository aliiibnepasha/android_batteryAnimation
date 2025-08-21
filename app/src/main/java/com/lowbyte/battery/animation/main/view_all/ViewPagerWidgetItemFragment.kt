package com.lowbyte.battery.animation.main.view_all

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.lowbyte.battery.animation.activity.BatteryWidgetEditApplyActivity
import com.lowbyte.battery.animation.adapter.AllWidgetAdapter
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.databinding.ItemViewPagerBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_LABEL
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_POSITION
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenHomeEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.widgetListAction
import com.lowbyte.battery.animation.utils.AnimationUtils.widgetListBasic
import com.lowbyte.battery.animation.utils.AnimationUtils.widgetListCute
import com.lowbyte.battery.animation.utils.AnimationUtils.widgetListFantasy
import com.lowbyte.battery.animation.utils.AnimationUtils.widgetListFashion
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class ViewPagerWidgetItemFragment : Fragment() {

    private lateinit var binding: ItemViewPagerBinding
    private lateinit var adapter: AllWidgetAdapter
    private var currentPos: Int = 0

    companion object {
        private const val ARG_POSITION = "position"

        fun newInstance(position: Int) = ViewPagerWidgetItemFragment().apply {
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

        // Log screen view event
        FirebaseAnalyticsUtils.logScreenView(this, "WidgetTab_$currentPos")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = AllWidgetAdapter { position, label, isRewarded ->
            // Log widget click event
            FirebaseAnalyticsUtils.logClickEvent(
                requireActivity(),
                "widget_selected",
                mapOf(
                    "tab_index" to currentPos.toString(),
                    "widget_label" to label,
                    "widget_position" to position.toString()
                )
            )



            AdManager.showInterstitialAd(
                requireActivity(),
                isFullscreenHomeEnabled,
                true
            ) {
                val intent = Intent(requireActivity(), BatteryWidgetEditApplyActivity::class.java).apply {
                    putExtra(EXTRA_POSITION, position)
                    putExtra(EXTRA_LABEL, label)
                    putExtra("RewardEarned", isRewarded)
                }
                startActivity(intent)
                FirebaseAnalyticsUtils.logClickEvent(
                    requireActivity(),
                    "BatteryWidgetEdit"
                )
            }



        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@ViewPagerWidgetItemFragment.adapter
        }


    }

    override fun onResume() {
        val widgetList = when (currentPos) {
            0 -> widgetListFantasy
            1 -> widgetListAction
            2 -> widgetListBasic
            3 -> widgetListCute
            4 -> widgetListFashion
            else -> emptyList()
        }

        Log.d("WidgetTab", "Tab $currentPos loaded with ${widgetList.size} items.")
        adapter.submitList(widgetList)
        super.onResume()
    }
}