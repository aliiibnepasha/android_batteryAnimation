import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lowbyte.battery.animation.adapter.ActionScrollAdapter
import com.lowbyte.battery.animation.adapter.ActionScrollItem
import com.lowbyte.battery.animation.databinding.FragmentGestureBottomSheetBinding

class GestureBottomSheetFragment(
    private val gestureTitle: String,
    private val gestureAction: String,
    private val actions: List<ActionScrollItem>
) : BottomSheetDialogFragment() {

    private var _binding: FragmentGestureBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGestureBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvGestureTitle.text = gestureTitle
        binding.tvGestureAction.text = gestureAction

        // RecyclerView setup
        binding.recyclerViewActions.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewActions.adapter = ActionScrollAdapter(actions)

        binding.btnClose.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}