package com.lowbyte.battery.animation.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.ActivityHowToUseBinding
import com.lowbyte.battery.animation.main.intro.SlidePagerAdapter

class HowToUseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHowToUseBinding
    private lateinit var adapter: SlidePagerAdapter
    private val slides = listOf("Page One", "Page Two", "Page Three")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  enableEdgeToEdge()

        binding = ActivityHowToUseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val sysInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(sysInsets.left, sysInsets.top, sysInsets.right, sysInsets.bottom)
            insets
        }

        adapter = SlidePagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.dotsIndicator.setViewPager2(binding.viewPager)

        binding.btnPrev.setOnClickListener {
            binding.viewPager.currentItem = binding.viewPager.currentItem - 1
        }

        binding.ibBackButton.setOnClickListener {
            finish()
        }




        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem < adapter.itemCount - 1){
                binding.viewPager.currentItem += 1
            } else{
                Toast.makeText(this, getString(R.string.got_it), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        updateButtons()  // initial state
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(pos: Int) { updateButtons() }
        })
    }


    private fun updateButtons() {
        val pos = binding.viewPager.currentItem
        binding.btnNext.text = if (pos == adapter.itemCount - 1) getString(R.string.got_it) else getString(R.string.next)
    }
}