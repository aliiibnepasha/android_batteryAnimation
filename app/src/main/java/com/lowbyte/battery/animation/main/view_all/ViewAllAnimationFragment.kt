package com.lowbyte.battery.animation.main.view_all

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.lowbyte.battery.animation.adapter.AnimationAdapter
import com.lowbyte.battery.animation.databinding.FragmentViewAllAnimationBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.animationList

class ViewAllAnimationFragment : Fragment() {
    private lateinit var binding: FragmentViewAllAnimationBinding
    private lateinit var adapter: AnimationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewAllAnimationBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = AnimationAdapter { position ->

        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@ViewAllAnimationFragment.adapter
        }

        // Add sample data
//        val items = List(14) {
//            "anim_${it + 1}"
//        }
//        Log.d("itemsLotti","${items}")
        adapter.submitList(animationList)
    }
}