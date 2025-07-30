package com.lowbyte.battery.animation.ui

import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.ActivityInteractiveLottieBinding
import com.lowbyte.battery.animation.serviceUtils.AllLottieAdapter
import com.lowbyte.battery.animation.serviceUtils.OnItemInteractionListener
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AppPreferences

class InteractiveLottieActivity : AppCompatActivity() {
    private lateinit var preferences: AppPreferences

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
        enableEdgeToEdge()
        setContentView(binding.root)
        preferences = AppPreferences.getInstance(this)
        binding.btnActivateSelected.text = if (preferences.getBoolean(
                "show_lottie_top_view",
                false
            ) == false
        ) getString(R.string.turn_off) else getString(R.string.turn_on)
        adapter = LottieItemAdapter(lottieItems) { resId ->
            binding.includeCanvas.lottieCanvas.removeItemByResId(resId)
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
                binding.includeCanvas.lottieCanvas.scaleSelectedItem(scale)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Rotation SeekBar
        binding.seekbarRotation.max = 360
        binding.seekbarRotation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.includeCanvas.lottieCanvas.rotateSelectedItem(progress.toFloat())
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

        binding.btnMoveTop.setOnClickListener {
            binding.includeCanvas.lottieCanvas.moveSelectedItem(
                0,
                -20
            )
        }
        binding.btnMoveBottom.setOnClickListener {
            binding.includeCanvas.lottieCanvas.moveSelectedItem(
                0,
                20
            )
        }
        binding.btnMoveLeft.setOnClickListener {
            binding.includeCanvas.lottieCanvas.moveSelectedItem(
                -20,
                0
            )
        }
        binding.btnMoveRight.setOnClickListener {
            binding.includeCanvas.lottieCanvas.moveSelectedItem(
                20,
                0
            )
        }
     //   binding.btnRemoveSelected.setOnClickListener { binding.lottieCanvas.removeSelectedItem() }

        // All Lotties
        binding.recyclerAllLotties.layoutManager = GridLayoutManager(this, 4)//LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val allLottieAdapter = AllLottieAdapter(availableLottieFiles) { resId ->
            if (!lottieItems.contains(resId) && lottieItems.size < 5) {
                binding.includeCanvas.lottieCanvas.addLottieItem(resId)
                lottieItems.add(resId)
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "Already added or limit reached", Toast.LENGTH_SHORT).show()
            }
        }
        binding.recyclerAllLotties.adapter = allLottieAdapter
        binding.includeCanvas.lottieCanvas.itemInteractionListener =
            object : OnItemInteractionListener {
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

        binding.btnActivateSelected.setOnClickListener {
            if (preferences.getBoolean("show_lottie_top_view") == true) {
                preferences.setBoolean("show_lottie_top_view", false)
                binding.btnActivateSelected.text = getString(R.string.turn_on)
            } else {
                preferences.setBoolean("show_lottie_top_view", true)
                binding.btnActivateSelected.text = getString(R.string.turn_off)

            }
            sendBroadcast(Intent(BROADCAST_ACTION))
        }
    }

}