package com.lowbyte.battery.animation.ui

import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.ActivityInteractiveLottieBinding
import com.lowbyte.battery.animation.serviceUtils.AllLottieAdapter
import com.lowbyte.battery.animation.serviceUtils.OnItemInteractionListener

class InteractiveLottieActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInteractiveLottieBinding
    private val lottieItems = mutableListOf<Int>() // store res ids

    private val availableLottieFiles = listOf(
        R.raw.ccc,
        R.raw.aaa,
        R.raw.ccc,
        R.raw.aaa,
        R.raw.bbb,
        R.raw.anim_7,
        R.raw.a_5,
        R.raw.a_12
    )

    private lateinit var adapter: LottieItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInteractiveLottieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = LottieItemAdapter(lottieItems) { resId ->
            binding.lottieCanvas.removeItemByResId(resId)
            lottieItems.remove(resId)
            adapter.notifyDataSetChanged()
        }

        binding.recyclerLotties.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerLotties.adapter = adapter

        // Size SeekBar
        binding.seekbarSize.max = 200
        binding.seekbarSize.progress = 100
        binding.seekbarSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val scale = progress / 100f
                binding.lottieCanvas.scaleSelectedItem(scale)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Rotation SeekBar
        binding.seekbarRotation.max = 360
        binding.seekbarRotation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.lottieCanvas.rotateSelectedItem(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

      //   Buttons
//        binding.btnAddLottie.setOnClickListener {
//            val resId = R.raw.ccc
//            if (!lottieItems.contains(resId)) {
//                binding.lottieCanvas.addLottieItem(resId)
//                lottieItems.add(resId)
//                adapter.notifyDataSetChanged()
//            }
//        }

        binding.btnMoveTop.setOnClickListener { binding.lottieCanvas.moveSelectedItem(0, -20) }
        binding.btnMoveBottom.setOnClickListener { binding.lottieCanvas.moveSelectedItem(0, 20) }
        binding.btnMoveLeft.setOnClickListener { binding.lottieCanvas.moveSelectedItem(-20, 0) }
        binding.btnMoveRight.setOnClickListener { binding.lottieCanvas.moveSelectedItem(20, 0) }
     //   binding.btnRemoveSelected.setOnClickListener { binding.lottieCanvas.removeSelectedItem() }

        // All Lotties
        binding.recyclerAllLotties.layoutManager = GridLayoutManager(this, 4)//LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val allLottieAdapter = AllLottieAdapter(availableLottieFiles) { resId ->
            if (!lottieItems.contains(resId) && lottieItems.size < 5) {
                binding.lottieCanvas.addLottieItem(resId)
                lottieItems.add(resId)
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "Already added or limit reached", Toast.LENGTH_SHORT).show()
            }
        }
        binding.recyclerAllLotties.adapter = allLottieAdapter
        binding.lottieCanvas.itemInteractionListener = object : OnItemInteractionListener {
            override fun onItemSelected(resId: Int?) {
//                if (resId == null) {
//                    Toast.makeText(this@InteractiveLottieActivity, "No item selected", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(this@InteractiveLottieActivity, "Selected ID: $resId", Toast.LENGTH_SHORT).show()
//                }
            }

            override fun onItemCountChanged(count: Int) {
                binding.tvAddEmoji.text = getString(R.string.sticker_added_5, count)
            }
        }
    }

}