package com.lowbyte.battery.animation.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.adapter.CustomIconGridAdapter
import com.lowbyte.battery.animation.databinding.ActivityStatusBarCustommizeBinding
import com.lowbyte.battery.animation.model.CustomIconGridItem

class StatusBarCustomizeActivity : AppCompatActivity() {

    private var _binding: ActivityStatusBarCustommizeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityStatusBarCustommizeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        _binding?.ibBackButton?.setOnClickListener {
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}