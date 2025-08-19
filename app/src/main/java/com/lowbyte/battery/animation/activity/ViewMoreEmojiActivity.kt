package com.lowbyte.battery.animation.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.databinding.ActivityViewMoreEmojiBinding
import com.lowbyte.battery.animation.dialoge.AccessibilityPermissionBottomSheet
import com.lowbyte.battery.animation.main.view_all.ViewPagerEmojiItemFragment
import com.lowbyte.battery.animation.server.EmojiViewModel
import com.lowbyte.battery.animation.server.EmojiViewModelFactory
import com.lowbyte.battery.animation.server.Resource
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AnimationUtils.applyTabMargins
import com.lowbyte.battery.animation.utils.AnimationUtils.dataUrl
import com.lowbyte.battery.animation.utils.AnimationUtils.getFullscreenHome2Id
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenStatusEnabled
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils
import com.lowbyte.battery.animation.utils.PermissionUtils.checkAccessibilityPermission
import com.lowbyte.battery.animation.utils.PermissionUtils.isAccessibilityServiceEnabled
import kotlinx.coroutines.launch

class ViewMoreEmojiActivity : BaseActivity() {

    private lateinit var binding: ActivityViewMoreEmojiBinding

    private lateinit var preferences: AppPreferences
    private lateinit var sheet: AccessibilityPermissionBottomSheet // Declare the sheet
    private val vm: EmojiViewModel by viewModels { EmojiViewModelFactory(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout using binding
        binding = ActivityViewMoreEmojiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AdManager.loadInterstitialAd(this,getFullscreenHome2Id(),isFullscreenStatusEnabled)
        binding.ibBackButton.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(
                this,
                "click_back_button",
                mapOf("screen" to "EmojiEditApplyScreen")
            )
            finish()
        }

        binding.ifvPro.setOnClickListener {
            startActivity(Intent(this, ProActivity::class.java))
        }


        preferences = AppPreferences.getInstance(this)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch {
                    vm.categories.collect { state ->
                        if (state is Resource.Success) {
                            val categoriesList = state.data
                            Log.d("APIData", "Categories List: $categoriesList")
                            setupViewPager(categoriesList,true)
                        }
                    }
                }
            }
        }
        vm.loadCategoryNames(dataUrl)

        sheet = AccessibilityPermissionBottomSheet(
            onAllowClicked = {
                FirebaseAnalyticsUtils.logClickEvent(
                    this,
                    "allow_accessibility_click"
                )
                startActivity(Intent(this, AllowAccessibilityActivity::class.java))
            },
            onCancelClicked = {
                FirebaseAnalyticsUtils.logClickEvent(
                    this,
                    "cancel_accessibility_permission"
                )
                preferences.isStatusBarEnabled = false
                binding.switchEnableBatteryEmojiViewAll.isChecked = false
            }, onDismissListener = {
                if (!isAccessibilityServiceEnabled()) {
                    preferences.isStatusBarEnabled = false
                    binding.switchEnableBatteryEmojiViewAll.isChecked = false
                }

            }
        )
        // Log screen view
        FirebaseAnalyticsUtils.logScreenView(this, "ViewAllEmojiFragment")
        // Apply system bar padding
//        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        binding.switchEnableBatteryEmojiViewAll.isChecked =
            preferences.isStatusBarEnabled &&isAccessibilityServiceEnabled()

        binding.switchEnableBatteryEmojiViewAll.setOnCheckedChangeListener { _, isChecked ->
            Handler(Looper.getMainLooper()).postDelayed({
                if (!isFinishing && !isDestroyed){
                    preferences.isStatusBarEnabled = isChecked
                    FirebaseAnalyticsUtils.logClickEvent(
                        this,
                        "toggle_statusbar_emoji_from_emoji_screen",
                        mapOf("enabled" to isChecked.toString())
                    )
                    if (preferences.isStatusBarEnabled && isChecked) {
                        checkAccessibilityPermission(false) {
                            when (it) {
                                "OpenBottomSheet" -> {
                                    sheet.show(supportFragmentManager, "AccessibilityPermission")
                                }

                                "Allowed" -> {
                                    binding.switchEnableBatteryEmojiViewAll.isChecked = preferences.isStatusBarEnabled
                                    sendBroadcast(Intent(BROADCAST_ACTION))
                                }

                                else -> {
                                    val existing =
                                        supportFragmentManager.findFragmentByTag("AccessibilityPermission")
                                    if (existing == null || !existing.isAdded) {
                                        sheet.show(supportFragmentManager, "AccessibilityPermission")
                                    } else {
                                        Log.d(
                                            "Accessibility",
                                            "AccessibilityPermissionBottomSheet already shown"
                                        )
                                    }
                                }
                            }

                        }
                    } else {
                        sendBroadcast(Intent(BROADCAST_ACTION))
                    }

                }

            }, 500)
        }

    }


    fun returnSelectedEmoji(emojiName: String, batteryName: String, positon: Int) {
        val resultIntent = Intent().apply {
            putExtra("emojiName", emojiName)
            putExtra("batteryName", batteryName)
            putExtra("positon", positon)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }


    private fun setupViewPager(tabsList: List<String>,isFromAllEmoji: Boolean) {
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = tabsList.size
            override fun createFragment(position: Int): Fragment {
                return ViewPagerEmojiItemFragment.newInstance(position, tabsList[position],isFromAllEmoji)
            }
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabsList[position]
        }.attach()

        applyTabMargins(binding.tabLayout, 8, 8)

    }

}
