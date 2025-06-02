import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lowbyte.battery.animation.adapter.ActionScrollAdapter
import com.lowbyte.battery.animation.adapter.ActionScrollItem
import com.lowbyte.battery.animation.databinding.FragmentGestureBottomSheetBinding
import com.lowbyte.battery.animation.utils.AppPreferences

class GestureBottomSheetFragment(
    private val gestureTitle: String,
    private val gestureAction: String,
    private val actions: List<ActionScrollItem>,
    private val onActionSelected: (String) -> Unit // ✅ New callback

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

          //  preferences.setString(gestureAction, label)
            Toast.makeText(context, "Action Applied - $label", Toast.LENGTH_SHORT).show()
            onActionSelected(label)
            dismiss() // Close the bottom sheet
        }

        binding.btnClose.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}