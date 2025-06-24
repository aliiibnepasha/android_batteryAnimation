import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
            Toast.makeText(context, getString(R.string.action_applied, label), Toast.LENGTH_SHORT).show()
            onActionSelected(actions[position])
          //  dismiss() // Close the bottom sheet
        }

        binding.btnClose.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}