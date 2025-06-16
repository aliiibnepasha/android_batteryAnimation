import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.activity.StatusBarGestureActivity
import com.lowbyte.battery.animation.adapter.ActionScrollAdapter
import com.lowbyte.battery.animation.adapter.ActionScrollItem
import com.lowbyte.battery.animation.databinding.FragmentGestureBottomSheetBinding
import com.lowbyte.battery.animation.utils.AppPreferences

class GestureBottomSheetFragment(
    private val gestureTitle: String,
    private val gestureAction: String,
    private val actions: List<ActionScrollItem>,
    private val onActionSelected: (ActionScrollItem) -> Unit

) : BottomSheetDialogFragment() {

    private var _binding: FragmentGestureBottomSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferences: AppPreferences



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGestureBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferences = AppPreferences.getInstance(requireContext())


        binding.tvGestureTitle.text = gestureTitle
        binding.tvGestureAction.text = gestureAction

        // RecyclerView setup
        binding.recyclerViewActions.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewActions.adapter = ActionScrollAdapter(actions){ position, label ->
            if (!preferences.isStatusBarEnabled ){
                Toast.makeText(requireContext(),
                    getString(R.string.please_enable_battery_emoji_service), Toast.LENGTH_LONG).show()

                return@ActionScrollAdapter
            }
          //  preferences.setString(gestureAction, label)
            Toast.makeText(context, getString(R.string.action_applied, label), Toast.LENGTH_SHORT).show()
            onActionSelected(actions[position])
            dismiss() // Close the bottom sheet
        }

        binding.btnClose.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}