package com.lowbyte.battery.animation.main.view_all

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.lowbyte.battery.animation.databinding.ItemViewPagerBinding

class ViewPagerItemFragment : Fragment() {
    private lateinit var binding: ItemViewPagerBinding
    private lateinit var adapter: AnimationAdapter

    companion object {
        fun newInstance(position: Int) = ViewPagerItemFragment().apply {
            arguments = Bundle().apply {
                putInt("position", position)
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
        adapter = AnimationAdapter { position ->
            // Handle item click
            // Navigate to detail screen or perform action
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@ViewPagerItemFragment.adapter
        }

        // Add sample data
        val items = List(10) { "Item ${it + 1}" }
        adapter.submitList(items)
    }
} 