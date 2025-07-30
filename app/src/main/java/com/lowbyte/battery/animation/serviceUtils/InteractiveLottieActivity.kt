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
import com.lowbyte.battery.animation.serviceUtils.LottieItem
import com.lowbyte.battery.animation.serviceUtils.OnItemInteractionListener
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.AppPreferences.Companion.KEY_SHOW_LOTTIE_TOP_VIEW

class InteractiveLottieActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInteractiveLottieBinding
    private lateinit var interactiveLottieView: InteractiveLottieView
    private lateinit var preferences: AppPreferences
    private lateinit var lotteSelectedAdapter: LottieItemAdapter

    private val lottieItems = ArrayList<LottieItem>() // For adapter display only
    private val availableLottieFiles = mutableListOf<Int>() // Declare empty list


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityInteractiveLottieBinding.inflate(layoutInflater)
        setContentView(binding.root)
        availableLottieFiles.addAll((1..56).map {
            resources.getIdentifier("lottie_$it", "raw", packageName)
        })
        preferences = AppPreferences.getInstance(this)
        interactiveLottieView = InteractiveLottieView(this)

        setupToggleButton()
        setupRecyclerViews()
        setupSeekBars()
        setupMovementControls()
        setupAllLotties()
        setupInteractionListener()
        loadItemsFromPreferences()
    }

    override fun onPause() {
        super.onPause()
        saveItemsToPreferences()
    }

    private fun setupToggleButton() {
        val isVisible = preferences.getBoolean(KEY_SHOW_LOTTIE_TOP_VIEW, false)?:false
        binding.btnActivateSelected.text = getString(if (isVisible) R.string.turn_off else R.string.turn_on)

        binding.btnActivateSelected.setOnClickListener {
            val currentState = preferences.getBoolean(KEY_SHOW_LOTTIE_TOP_VIEW, false)
            val newState = !(currentState ?: true)
            preferences.setBoolean(KEY_SHOW_LOTTIE_TOP_VIEW, newState)
            binding.btnActivateSelected.text = getString(
                if (newState) R.string.turn_off else R.string.turn_on
            )

            sendBroadcast(Intent(BROADCAST_ACTION))
        }
    }

    private fun setupRecyclerViews() {
        lotteSelectedAdapter = LottieItemAdapter(lottieItems) { lottieItem ->
            interactiveLottieView.removeItemByResId(lottieItem.resId)
           // lottieItems.remove(lottieItem)
         //   lotteSelectedAdapter.updateItems(lottieItems)
        }

        binding.recyclerLotties.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerLotties.adapter = lotteSelectedAdapter
    }

    private fun setupSeekBars() {
        binding.seekbarSize.max = 200
        binding.seekbarSize.progress = 100
        binding.seekbarSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                interactiveLottieView.scaleSelectedItem(progress / 100f)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.seekbarRotation.max = 360
        binding.seekbarRotation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                interactiveLottieView.rotateSelectedItem(progress.toFloat())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupMovementControls() {
        binding.btnMoveTop.setOnClickListener { interactiveLottieView.moveSelectedItem(0, -10) }
        binding.btnMoveBottom.setOnClickListener { interactiveLottieView.moveSelectedItem(0, 10) }
        binding.btnMoveLeft.setOnClickListener { interactiveLottieView.moveSelectedItem(-10, 0) }
        binding.btnMoveRight.setOnClickListener { interactiveLottieView.moveSelectedItem(10, 0) }
    }

    private fun setupAllLotties() {
        binding.recyclerAllLotties.layoutManager = GridLayoutManager(this, 4)
        val allLottieAdapter = AllLottieAdapter(availableLottieFiles) { resId ->
            if (interactiveLottieView.containsItem(resId)) {
                Toast.makeText(this, "Already added", Toast.LENGTH_SHORT).show()
                return@AllLottieAdapter
            }

            if (lottieItems.size >= 5) {
                Toast.makeText(this, "Limit reached", Toast.LENGTH_SHORT).show()
                return@AllLottieAdapter
            }

            interactiveLottieView.addLottieItem(resId)
        }
        binding.recyclerAllLotties.adapter = allLottieAdapter
    }

    private fun setupInteractionListener() {
        interactiveLottieView.itemInteractionListener = object : OnItemInteractionListener {
            override fun onItemSelected(resId: Int?) {
                // Optional: handle selected item
            }

            override fun onItemCountChanged(items: List<LottieItem>) {

                lotteSelectedAdapter.updateItems(items)
                binding.tvAddEmoji.text = getString(R.string.sticker_added_5, items.size)
            }
        }
    }

    private fun saveItemsToPreferences() {
        val dataList = interactiveLottieView.getCurrentLottieItemData()
        preferences.putLottieItemList("lottie_item_list", dataList)
    }

    private fun loadItemsFromPreferences() {
        val savedItems = preferences.getLottieItemList("lottie_item_list")
        savedItems.forEach { interactiveLottieView.addLottieItemFromData(it) }
    }
}