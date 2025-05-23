import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.adapter.LanguageAdapter

class LanguageFragment : Fragment(R.layout.fragment_language) {

    private lateinit var adapter: LanguageAdapter
    private var selectedLanguage: String = "English"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val languages = listOf("English", "Spanish", "French", "Hindi", "Arabic", "Spanish", "French", "Hindi", "Arabic")
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val buttonBack = view.findViewById<ImageFilterView>(R.id.ibBackButton)
        val buttonNext = view.findViewById<TextView>(R.id.ibNextButton)
        buttonBack.setOnClickListener {
            requireActivity().finish()
        }
        buttonNext.setOnClickListener {
            findNavController().navigate(R.id.action_language_to_intro)

        }



        adapter = LanguageAdapter(languages) { language ->
            selectedLanguage = language

        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
    }
}