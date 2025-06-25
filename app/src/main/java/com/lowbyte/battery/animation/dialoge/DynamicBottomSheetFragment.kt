import android.Manifest
import android.content.pm.PackageManager
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
        binding.recyclerViewActions.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewActions.adapter = ActionDynamicAdapter(actions){ position, label ->
            if (!preferences.isDynamicEnabled ){
                Toast.makeText(requireContext(),
                    getString(R.string.please_enable_battery_emoji_service), Toast.LENGTH_LONG).show()
                return@ActionDynamicAdapter
            }
            if (checkAndRequestBluetoothPermission()){
                Log.d("Bluetooth", "BLUETOOTH_CONNECT permission granted")
            }else{
                return@ActionDynamicAdapter
            }
            Toast.makeText(context, getString(R.string.action_applied, label), Toast.LENGTH_SHORT).show()
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
                bluetoothPermissionRequest.launch(Manifest.permission.BLUETOOTH_CONNECT)
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




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}