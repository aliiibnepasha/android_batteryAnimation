package com.lowbyte.battery.animation.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.adapter.CustomIconGridAdapter
import com.lowbyte.battery.animation.databinding.ActivityStatusBarCustommizeBinding
import com.lowbyte.battery.animation.model.CustomIconGridItem
import com.lowbyte.battery.animation.utils.AppPreferences

class StatusBarCustomizeActivity : AppCompatActivity() {

    private var _binding: ActivityStatusBarCustommizeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityStatusBarCustommizeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val prefs = AppPreferences.getInstance(this)


        _binding?.ibBackButton?.setOnClickListener {
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

// Restore previous values
        binding.statusBarHeightSeekbar.progress = prefs.statusBarHeight
        binding.leftMarginSeekBar.progress = prefs.statusBarMarginLeft
        binding.rightMarginSeekBar.progress = prefs.statusBarMarginRight

        val minStatusBarHeightPx = getSystemStatusBarHeight(this) / resources.displayMetrics.density // get in dp

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.statusBarHeightSeekbar.min = minStatusBarHeightPx.toInt()
        }
        binding.statusBarHeightSeekbar.progress = maxOf(prefs.statusBarHeight, minStatusBarHeightPx.toInt())
        binding.statusBarHeight.text = "${binding.statusBarHeightSeekbar.progress} dp"

        binding.statusBarHeightSeekbar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                val minStatusBarHeightDp = minStatusBarHeightPx.toInt()
                val safeProgress = progress.coerceAtLeast(minStatusBarHeightDp)
                prefs.statusBarHeight = safeProgress
                binding.statusBarHeight.text = "$safeProgress dp"
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })


        binding.leftMarginSeekBar.setOnSeekBarChangeListener(object :
            android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: android.widget.SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                prefs.statusBarMarginLeft = progress
                binding.longTapLabel.text = "${prefs.statusBarMarginLeft} dp"
                Log.d("servicesdd", "Broadcast sent ff!")
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        binding.rightMarginSeekBar.setOnSeekBarChangeListener(object :
            android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: android.widget.SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                prefs.statusBarMarginRight = progress
                binding.swipeLeftToRightTapLabel.text = "${prefs.statusBarMarginLeft} dp"
                Log.d("servicesdd", "Broadcast sent gg!")
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))

            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })






        // Create dummy data
        val items = ArrayList<CustomIconGridItem>().apply {
            add(CustomIconGridItem(R.drawable.ic_signal_wifi, getString(R.string.wifi)))
            add(CustomIconGridItem(R.drawable.ic_signal_date, getString(R.string.data)))
            add(CustomIconGridItem(R.drawable.ic_signal_mobile, getString(R.string.signals)))
            add(CustomIconGridItem(R.drawable.ic_airplan_mod, getString(R.string.airplane)))
            add(CustomIconGridItem(R.drawable.ic_signal_hotspot, getString(R.string.hotspot)))
            add(CustomIconGridItem(R.drawable.ic_time_date, getString(R.string.time)))
        }

        val adapter = CustomIconGridAdapter(items) { position, label ->
            val intent = Intent(this, StatusBarIconSettingsActivity::class.java)
            intent.putExtra("EXTRA_POSITION", position)
            intent.putExtra("EXTRA_LABEL", label)
            startActivity(intent)
        }
        binding.recyclerViewCustomIcon.adapter = adapter
        binding.recyclerViewCustomIcon.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerViewCustomIcon.adapter = adapter
    }
    fun getSystemStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            // fallback 24dp
            (24 * context.resources.displayMetrics.density).toInt()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}