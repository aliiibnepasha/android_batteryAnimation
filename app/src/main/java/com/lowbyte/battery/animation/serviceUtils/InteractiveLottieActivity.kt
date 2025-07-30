package com.lowbyte.battery.animation.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.lowbyte.battery.animation.serviceUtils.LottieItem
import com.lowbyte.battery.animation.serviceUtils.OnItemInteractionListener
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.AppPreferences.Companion.KEY_SHOW_LOTTIE_TOP_VIEW

class InteractiveLottieActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInteractiveLottieBinding
    private lateinit var interactiveLottieView: InteractiveLottieView
    private lateinit var preferences: AppPreferences

    private val lottieItems = ArrayList<LottieItem>()  // Track added resIds
    private lateinit var adapter: LottieItemAdapter

    private val availableLottieFiles = listOf(
        R.raw.lottie_1,
        R.raw.lottie_2,
        R.raw.lottie_3,
        R.raw.lottie_4,
        R.raw.lottie_5,
        R.raw.lottie_6,
        R.raw.lottie_7,
        R.raw.lottie_8,
        R.raw.lottie_9,
        R.raw.lottie_10,
        R.raw.lottie_11,
        R.raw.lottie_12,
        R.raw.lottie_13,
        R.raw.lottie_14,
        R.raw.lottie_15,
        R.raw.lottie_16,
        R.raw.lottie_17,
        R.raw.lottie_18,
        R.raw.lottie_19,
        R.raw.lottie_20,
        R.raw.lottie_21,
        R.raw.lottie_22,
        R.raw.lottie_23,
        R.raw.lottie_24,
        R.raw.lottie_25,
        R.raw.lottie_26,
        R.raw.lottie_27,
        R.raw.lottie_28,
        R.raw.lottie_29,
        R.raw.lottie_30,
        R.raw.lottie_31,
        R.raw.lottie_32,
        R.raw.lottie_33,
        R.raw.lottie_34,
        R.raw.lottie_35,
        R.raw.lottie_36,
        R.raw.lottie_37,
        R.raw.lottie_38,
        R.raw.lottie_39,
        R.raw.lottie_40,
        R.raw.lottie_41,
        R.raw.lottie_42,
        R.raw.lottie_43,
        R.raw.lottie_44,
        R.raw.lottie_45,
        R.raw.lottie_46,
        R.raw.lottie_47,
        R.raw.lottie_48,
        R.raw.lottie_49,
        R.raw.lottie_50,
        R.raw.lottie_51,
        R.raw.lottie_52,
        R.raw.lottie_53,
        R.raw.lottie_54,
        R.raw.lottie_55,
        R.raw.lottie_56,

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
        adapter = LottieItemAdapter(lottieItems) { lottieItem ->
            interactiveLottieView.removeItemByResId(lottieItem)
            interactiveLottieView.removeSelectedItem()
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
            if (/*!lottieItems.contains(resId) && */lottieItems.size < 5) {
                interactiveLottieView.addLottieItem(resId)
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

            override fun onItemCountChanged(items: List<LottieItem>) {
              //  lottieItems.clear()
                lottieItems.addAll(items)
                Log.d("LottieItemAdapter", "onBindViewHolderSize: ${lottieItems.size}")

                adapter.updateItems(lottieItems)
                binding.tvAddEmoji.text = getString(R.string.sticker_added_5, items.size)

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