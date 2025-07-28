
package com.lowbyte.battery.animation.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.custom.InteractiveLottieView

class InteractiveLottieActivity : AppCompatActivity() {

    private lateinit var lottieView: InteractiveLottieView
    private lateinit var sizeSeekBar: SeekBar
    private lateinit var rotationSeekBar: SeekBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LottieItemAdapter
    private val lottieItems = mutableListOf<Int>() // store res ids

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interactive_lottie)

        lottieView = findViewById(R.id.lottie_canvas)
        sizeSeekBar = findViewById(R.id.size_seekbar)
        rotationSeekBar = findViewById(R.id.rotation_seekbar)
        recyclerView = findViewById(R.id.recycler_lotties)

        adapter = LottieItemAdapter(lottieItems) { resId ->
            lottieView.removeSelectedItem()
            lottieItems.remove(resId)
            adapter.notifyDataSetChanged()
        }

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter

        sizeSeekBar.max = 200
        sizeSeekBar.progress = 100
        sizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val scale = progress / 100f
                lottieView.scaleSelectedItem(scale)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        rotationSeekBar.max = 360
        rotationSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                lottieView.rotateSelectedItem(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        findViewById<Button>(R.id.btn_add_lottie).setOnClickListener {
            val resId = R.raw.a_1 // Replace with actual animation resource
            if (!lottieItems.contains(resId)) {
                lottieView.addLottieItem(resId)
                lottieItems.add(resId)
                adapter.notifyDataSetChanged()
            }
        }

        findViewById<Button>(R.id.btn_move_top).setOnClickListener { lottieView.moveSelectedItem(0, -20) }
        findViewById<Button>(R.id.btn_move_bottom).setOnClickListener { lottieView.moveSelectedItem(0, 20) }
        findViewById<Button>(R.id.btn_move_left).setOnClickListener { lottieView.moveSelectedItem(-20, 0) }
        findViewById<Button>(R.id.btn_move_right).setOnClickListener { lottieView.moveSelectedItem(20, 0) }
        findViewById<Button>(R.id.btn_remove_selected).setOnClickListener {
            lottieView.removeSelectedItem()
        }
    }
}
