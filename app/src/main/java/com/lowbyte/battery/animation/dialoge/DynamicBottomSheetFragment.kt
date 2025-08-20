import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.adapter.ActionDynamicAdapter
import com.lowbyte.battery.animation.adapter.ActionDynamicItem
import com.lowbyte.battery.animation.databinding.DialogBlutoothPermissionBinding
import com.lowbyte.battery.animation.databinding.DialogNotificationPermissionBinding
import com.lowbyte.battery.animation.databinding.DialogRationaleBinding
import com.lowbyte.battery.animation.databinding.FragmentDynamicBottomSheetBinding
import com.lowbyte.battery.animation.utils.AppPreferences

class DynamicBottomSheetFragment(
    private val actions: List<ActionDynamicItem>,
    private val onActionSelected: (ActionDynamicItem) -> Unit

) : BottomSheetDialogFragment() {
    private var _binding: FragmentDynamicBottomSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferences: AppPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDynamicBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferences = AppPreferences.getInstance(requireContext())

        // RecyclerView setup
        binding.indicatorClose.setOnClickListener {
            dismiss()
        }
        binding.recyclerViewActions.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewActions.adapter =
            ActionDynamicAdapter(actions) { position, label, actionName, isChecked ->
            if (!preferences.isDynamicEnabled ){
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_enable_battery_emoji_service),
                    Toast.LENGTH_LONG
                ).show()
                return@ActionDynamicAdapter
            }
                when (actionName) {
                    "switch_notification" -> {
                        if (isNotificationServiceEnabled(requireContext())) {
                            Log.d(
                                "Notification Service",
                                "Notification service enabled switch_notification permission granted"
                            )
                        } else {
                            showNotificationPermissionDialog(requireContext())
                            dismiss()
                            return@ActionDynamicAdapter
                        }
                    }

                    "switch_bluetooth" -> {
                        if (checkAndRequestBluetoothPermission()) {
                            Log.d("Bluetooth", "BLUETOOTH_CONNECT permission granted")
                        } else {
                            dismiss()
                            return@ActionDynamicAdapter
                        }
                    }
            }
                preferences.setBoolean(actionName, isChecked)
                if (isChecked) {
                    Toast.makeText(
                        context,
                        getString(R.string.action_applied, label),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            onActionSelected(actions[position])
          //  dismiss() // Close the bottom sheet
        }

        binding.btnClose.setOnClickListener { dismiss() }
    }
    private val bluetoothPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("Bluetooth", "BLUETOOTH_CONNECT permission granted")
                // You can now safely use Bluetooth features
            } else {
                Toast.makeText(requireContext(), "Bluetooth permission required", Toast.LENGTH_LONG).show()
            }
        }

    private fun checkAndRequestBluetoothPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                showCustomPermissionDialog()
                return false
            } else {
                // Permission already granted
                return true
            }
        } else {
            // No need to request for below Android 12
            return true
        }
    }


    fun isNotificationServiceEnabled(context: Context): Boolean {
        val enabledListeners = android.provider.Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return enabledListeners?.contains(context.packageName) == true
    }

    fun requestNotificationAccess(context: Context) {
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    private fun showNotificationPermissionDialog(context: Context) {
        val dialog = Dialog(requireContext())
        val binding = DialogNotificationPermissionBinding.inflate(layoutInflater)

        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

//        // Set title & body dynamically if needed
//        binding.tvTitle.text = getString(R.string.enable_notifications)
//        binding.tvBody.text = getString(R.string.notification_mesg)

        // Listeners
        binding.btnAllow.setOnClickListener {
            dialog.dismiss()
            requestNotificationAccess(context)
        }

        binding.btnDeny.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun showCustomPermissionDialog() {
        val dialog = Dialog(requireContext())
        val binding = DialogBlutoothPermissionBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        // Set title & body dynamically if needed
//        binding.tvTitle.text = getString(R.string.enable_notifications)
//        binding.tvBody.text = getString(R.string.notification_mesg)

        // Listeners
        binding.btnAllow.setOnClickListener {
            dialog.dismiss()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                bluetoothPermissionRequest.launch(Manifest.permission.BLUETOOTH_CONNECT)

            }
        }

        binding.btnDeny.setOnClickListener {
            dialog.dismiss()

        }

        dialog.show()
    }


    private fun showCustomRationaleDialog() {
        val dialog = Dialog(requireContext())
        val binding = DialogRationaleBinding.inflate(layoutInflater)

        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        // Set title & body dynamically if needed
//        binding.tvTitle.text = getString(R.string.why_we_need_notification_access)
//        binding.tvBody.text = getString(R.string.notification_mesg_2)

        // Listeners
        binding.btnOpenSettings.setOnClickListener {
            dialog.dismiss()
            val intent =
                Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                }
            startActivity(intent)
        }

        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}