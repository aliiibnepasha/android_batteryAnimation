
package com.lowbyte.battery.animation.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.custom.InteractiveLottieView
import com.lowbyte.battery.animation.serviceUtils.AllLottieAdapter

class InteractiveLottieActivity : AppCompatActivity() {
    private val availableLottieFiles = listOf(
        R.raw.ccc,
        R.raw.aaa,
        R.raw.bbb,
        R.raw.anim_7,
        R.raw.a_5,
        R.raw.a_12
    )


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
            val resId = R.raw.ccc // Replace with actual animation resource
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


        val allRecyclerView = findViewById<RecyclerView>(R.id.recycler_all_lotties)
        allRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val allLottieAdapter = AllLottieAdapter(availableLottieFiles) { resId ->
            if (!lottieItems.contains(resId) && lottieItems.size < 5) {
                lottieView.addLottieItem(resId)
                lottieItems.add(resId)
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "Already added or limit reached", Toast.LENGTH_SHORT).show()
            }
        }
        allRecyclerView.adapter = allLottieAdapter
    }
}
