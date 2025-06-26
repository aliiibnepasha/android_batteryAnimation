package com.lowbyte.battery.animation.main.island

import DynamicBottomSheetFragment
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.lowbyte.battery.animation.BuildConfig
import com.lowbyte.battery.animation.NotchAccessibilityService
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.activity.AllowAccessibilityActivity
import com.lowbyte.battery.animation.adapter.ActionDynamicItem
import com.lowbyte.battery.animation.databinding.FragmentIslandBinding
import com.lowbyte.battery.animation.dialoge.AccessibilityPermissionBottomSheet
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class IslandFragment : Fragment() {

    private var _binding: FragmentIslandBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferences: AppPreferences
    private lateinit var sheet: AccessibilityPermissionBottomSheet // Declare the sheet

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIslandBinding.inflate(inflater, container, false)
        val root: View = binding.root
        preferences = AppPreferences.getInstance(requireContext())
        FirebaseAnalyticsUtils.logScreenView(this, "IslandFragment")



        if (preferences.getInt("widget_style_index", 0) == 0) {
            binding.selectNotchStyle1.visibility = View.VISIBLE
            binding.selectNotchStyle2.visibility = View.GONE
        } else {
            binding.selectNotchStyle1.visibility = View.GONE
            binding.selectNotchStyle2.visibility = View.VISIBLE
        }

       /*............................................*/
        binding.yAxisSeekbar.max = 100
        binding.yAxisSeekbar.progress = preferences.notchYAxis + 50

        binding.yAxisSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val realValue = progress - 50 // converts SeekBar progress back to real value
                preferences.notchYAxis = realValue
                Log.d("TAG_Access", "notchYAxis $realValue")

                FirebaseAnalyticsUtils.logClickEvent(
                    this@IslandFragment, "notchYAxis", mapOf("value" to realValue.toString())
                )

                requireContext().sendBroadcast(Intent(BROADCAST_ACTION))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (!binding.switchEnableDynamic.isChecked) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.please_enable_dynamic_mode),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })

       /*............................................*/
        binding.xAxisSeekbar.max = 400
        binding.xAxisSeekbar.progress = preferences.notchXAxis + 200
        binding.xAxisSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val realValue = progress - 200 // center is now 0
                preferences.notchXAxis = realValue
                Log.d("TAG_Access", "notchXAxis $realValue")

                requireContext().sendBroadcast(Intent(BROADCAST_ACTION))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (!binding.switchEnableDynamic.isChecked) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.please_enable_dynamic_mode),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })

       /*......... ...................................*/
        binding.heightSeekbar.max = 100
        binding.heightSeekbar.progress = preferences.notchHeight
        binding.heightSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val safeProgress = progress.coerceIn(10, 100)
                preferences.notchHeight = safeProgress
                Log.d("TAG_Access", "notchHeight $safeProgress")
                requireContext().sendBroadcast(Intent(BROADCAST_ACTION))

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (!binding.switchEnableDynamic.isChecked) {
                    Toast.makeText(requireContext(), getString(R.string.please_enable_dynamic_mode), Toast.LENGTH_LONG).show()
                }
            }
        })

       /*......... ...................................*/
        binding.widthSeekbar.max = 200
        binding.widthSeekbar.progress = preferences.notchWidth
        binding.widthSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val safeProgress = progress.coerceIn(20, 200)
                preferences.notchWidth = safeProgress
                Log.d("TAG_Access", "notchWidth $safeProgress")
                requireContext().sendBroadcast(Intent(BROADCAST_ACTION))

            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (!binding.switchEnableDynamic.isChecked) {
                    Toast.makeText(requireContext(), getString(R.string.please_enable_dynamic_mode), Toast.LENGTH_LONG).show()
                }
            }
        })

        binding.btnPositionReset.setOnClickListener {
            if (!preferences.isDynamicEnabled) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_enable_dynamic_mode),
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener

            } else {
                binding.xAxisSeekbar.progress = 200  // for notchXAxis = 0
                binding.yAxisSeekbar.progress = 54   // for notchYAxis = 0
                Toast.makeText(
                    requireContext(), getString(R.string.position_reset), Toast.LENGTH_SHORT
                ).show()
            }


        }

        binding.btnResetSize.setOnClickListener {
            if (!preferences.isDynamicEnabled) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_enable_dynamic_mode),
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener

            } else {
                val screenWidth = Resources.getSystem().displayMetrics.widthPixels
                binding.widthSeekbar.progress = screenWidth / 8
                binding.heightSeekbar.progress = 25
                Toast.makeText(requireContext(), getString(R.string.size_reset), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.notchStyle2.setOnClickListener {
            if (!preferences.isDynamicEnabled) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_enable_dynamic_mode),
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            binding.selectNotchStyle2.visibility = View.VISIBLE
            binding.selectNotchStyle1.visibility = View.GONE
            preferences.setInt("widget_style_index", 1)
            requireContext().sendBroadcast(Intent(BROADCAST_ACTION))


        }
        binding.notchStyle1.setOnClickListener {
            if (!preferences.isDynamicEnabled) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_enable_dynamic_mode),
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            binding.selectNotchStyle1.visibility = View.VISIBLE
            binding.selectNotchStyle2.visibility = View.GONE
            preferences.setInt("widget_style_index", 0)
            requireContext().sendBroadcast(Intent(BROADCAST_ACTION))



        }

        binding.viewHowToUse.setOnClickListener {
            if (!preferences.isDynamicEnabled) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_enable_dynamic_mode),
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            openDynamicSheet()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            binding.switchEnableDynamic.isChecked = preferences.isDynamicEnabled && isAccessibilityServiceEnabled()
            Log.d("TAG_Access", "Create ${preferences.isDynamicEnabled}")
            binding.switchEnableDynamic.setOnCheckedChangeListener { _, isChecked ->
                preferences.isDynamicEnabled = isChecked
                FirebaseAnalyticsUtils.logClickEvent(
                    requireActivity(), "toggle_dynamic_service",
                    mapOf("enabled" to isChecked.toString())
                )
                if (::preferences.isInitialized && preferences.isDynamicEnabled && isChecked) {
                    checkAccessibilityPermission()
                } else {
                    requireActivity().sendBroadcast(Intent(BROADCAST_ACTION))
                }
            }
        }, 500)
        binding.switchEnableDynamic.isChecked = preferences.isDynamicEnabled
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sheet = AccessibilityPermissionBottomSheet(
            onAllowClicked = {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_enable_accessibility_service),
                    Toast.LENGTH_LONG
                ).show()
                FirebaseAnalyticsUtils.logClickEvent(
                    requireActivity(),
                    "accessibility_allow_clicked"
                )
                startActivity(Intent(requireActivity(), AllowAccessibilityActivity::class.java))
            },
            onCancelClicked = {
                FirebaseAnalyticsUtils.logClickEvent(
                    requireActivity(),
                    "accessibility_cancel_clicked"
                )
                preferences.isDynamicEnabled = false
                binding.switchEnableDynamic.isChecked = false
            },
            onDismissListener = {
                if (!isAccessibilityServiceEnabled()) {
                    preferences.isDynamicEnabled = false
                    binding.switchEnableDynamic.isChecked = false
                }
            }
        )
    }

    private fun openDynamicSheet() {
        FirebaseAnalyticsUtils.logClickEvent(
            this,
            "dynamic_bottomSheet_opened",
            mapOf("dynamic" to "dynamicKey")
        )

        val items = listOf(
            ActionDynamicItem(getString(R.string.notification), "switch_notification"),
            ActionDynamicItem(getString(R.string.music), "switch_music"),
            ActionDynamicItem(getString(R.string.navigation), "switch_navigation"),
            ActionDynamicItem(getString(R.string.battery), "switch_battery"),//
            ActionDynamicItem(getString(R.string.bluetooth), "switch_bluetooth"),//
            ActionDynamicItem(getString(R.string.mute), "switch_mute"),//
        )

        DynamicBottomSheetFragment(items) { selected ->
            FirebaseAnalyticsUtils.logClickEvent(
                this,
                "dynamic_action_selected",
                mapOf("dynamic" to "dynamicKey", "action" to selected.actionName)
            )
        }.show(childFragmentManager, "DynamicBottomSheet")

    }

    private fun checkAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled()) {
            FirebaseAnalyticsUtils.logClickEvent(requireActivity(), "accessibility_prompt_shown")
            if (BuildConfig.DEBUG){
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }else{
                sheet.show(childFragmentManager, "AccessibilityPermission")
            }
        } else {
            binding.switchEnableDynamic.isChecked = preferences.isDynamicEnabled
            requireActivity().sendBroadcast(Intent(BROADCAST_ACTION))
        }
    }


    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponentName =
            "${requireContext().packageName}/${NotchAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            requireContext().contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabledServices.split(':').any {
            it.equals(expectedComponentName, ignoreCase = true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        if (!isAccessibilityServiceEnabled()) {
            binding.switchEnableDynamic.isChecked = false
            preferences.isDynamicEnabled = false
        }
        super.onResume()
    }
}