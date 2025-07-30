package com.lowbyte.battery.animation.ui

import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.custom.InteractiveLottieView
import com.lowbyte.battery.animation.databinding.ActivityInteractiveLottieBinding
import com.lowbyte.battery.animation.serviceUtils.AllLottieAdapter
import com.lowbyte.battery.animation.serviceUtils.OnItemInteractionListener
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.AppPreferences.Companion.KEY_SHOW_LOTTIE_TOP_VIEW

class InteractiveLottieActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInteractiveLottieBinding
    private lateinit var interactiveLottieView: InteractiveLottieView
    private lateinit var preferences: AppPreferences

    private val lottieItems = mutableListOf<Int>()  // Track added resIds
    private lateinit var adapter: LottieItemAdapter

    private val availableLottieFiles = listOf(
        R.raw.ccc,
        R.raw.aaa,
        R.raw.swing,
        R.raw.bbb,
        R.raw.anim_7,
        R.raw.a_5,
        R.raw.a_12
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityInteractiveLottieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = AppPreferences.getInstance(this)
        interactiveLottieView = InteractiveLottieView(this)

        // Set toggle button text
        binding.btnActivateSelected.text =
            if (preferences.getBoolean(KEY_SHOW_LOTTIE_TOP_VIEW, false)==false)
                getString(R.string.turn_off) else getString(R.string.turn_on)

        // Setup adapter for selected Lottie items
        adapter = LottieItemAdapter(lottieItems) { resId ->
            interactiveLottieView.removeItemByResId(resId)
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
                interactiveLottieView.scaleSelectedItem(scale)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Rotation SeekBar
        binding.seekbarRotation.max = 360
        binding.seekbarRotation.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                interactiveLottieView.rotateSelectedItem(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Movement buttons
        binding.btnMoveTop.setOnClickListener {
            interactiveLottieView.moveSelectedItem(0,-10)
        }

        binding.btnMoveBottom.setOnClickListener {
            interactiveLottieView.moveSelectedItem(0, 10)
        }

        binding.btnMoveLeft.setOnClickListener {
            interactiveLottieView.moveSelectedItem(-10,0)
        }

        binding.btnMoveRight.setOnClickListener {
            interactiveLottieView.moveSelectedItem(10,0)

        }




        // Remove selected item
//        binding.btnRemoveSelected.setOnClickListener {
//            interactiveLottieView.removeSelectedItem()
//        }

        // Load all Lotties
        binding.recyclerAllLotties.layoutManager = GridLayoutManager(this, 4)
        val allLottieAdapter = AllLottieAdapter(availableLottieFiles) { resId ->
            if (!lottieItems.contains(resId) && lottieItems.size < 5) {
                interactiveLottieView.addLottieItem(resId)
                lottieItems.add(resId)
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "Already added or limit reached", Toast.LENGTH_SHORT).show()
            }
        }
        binding.recyclerAllLotties.adapter = allLottieAdapter

        // Set listener for item events
        interactiveLottieView.itemInteractionListener = object : OnItemInteractionListener {
            override fun onItemSelected(resId: Int?) {
                // Optional: show Toast or update UI
            }

            override fun onItemCountChanged(count: Int) {
                binding.tvAddEmoji.text = getString(R.string.sticker_added_5, count)
            }
        }

        // Toggle Lottie top view
        binding.btnActivateSelected.setOnClickListener {
            if (preferences.getBoolean(KEY_SHOW_LOTTIE_TOP_VIEW) == true) {
                preferences.setBoolean(KEY_SHOW_LOTTIE_TOP_VIEW, false)
                binding.btnActivateSelected.text = getString(R.string.turn_on)
            } else {
                preferences.setBoolean(KEY_SHOW_LOTTIE_TOP_VIEW, true)
                binding.btnActivateSelected.text = getString(R.string.turn_off)

                sendBroadcast(Intent(BROADCAST_ACTION))
            }
        }
    }
}