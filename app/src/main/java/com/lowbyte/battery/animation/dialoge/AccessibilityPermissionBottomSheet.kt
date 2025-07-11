package com.lowbyte.battery.animation.dialoge

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.adapter.BulletAdapter
import com.lowbyte.battery.animation.databinding.AccessibilityPermissionDialogBinding
import com.lowbyte.battery.animation.model.BulletItem

class AccessibilityPermissionBottomSheet(
    private val onAllowClicked: () -> Unit,
    private val onCancelClicked: () -> Unit,
    var onDismissListener: (() -> Unit)? = null
) : BottomSheetDialogFragment() {

    private var _binding: AccessibilityPermissionDialogBinding? = null
    private val binding get() = _binding!!

    private var isTermsAccepted = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AccessibilityPermissionDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Toggle listener
        binding.checkTerms.setOnClickListener {
            isTermsAccepted = !isTermsAccepted
            updateToggleUI()
        }

        val recyclerView = binding.recyclerForDisclaimer
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val items = listOf(
            BulletItem.Title(getString(R.string.service_title_1)),
            BulletItem.Description(getString(R.string.service_title_1_1)),
            BulletItem.Description(getString(R.string.service_title_1_2)),
            BulletItem.Description(getString(R.string.service_title_1_3)),
            BulletItem.Description(getString(R.string.service_title_1_4)),
            BulletItem.Title(getString(R.string.service_title_2)),
            BulletItem.Description(getString(R.string.service_title_2_1)),
            BulletItem.Description(getString(R.string.service_title_2_2)),
            BulletItem.Description(getString(R.string.service_title_2_3)),
            BulletItem.Title(getString(R.string.service_title_3)),
            BulletItem.Description(getString(R.string.service_title_3_1)),
            BulletItem.Description(getString(R.string.service_title_3_2)),
            BulletItem.Description(getString(R.string.service_title_3_3)),
            BulletItem.Title(getString(R.string.service_title_4)),
            BulletItem.Description(getString(R.string.service_title_4_1)),
            BulletItem.Description(getString(R.string.service_title_4_2)),
            BulletItem.Description(getString(R.string.service_title_4_3)),
        )

        val adapter = BulletAdapter(items)
        recyclerView.adapter = adapter



        binding.btnAllowTxt.setOnClickListener {
            if (isTermsAccepted) {
                onAllowClicked.invoke()
                dismiss()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_accept_terms_of_service),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnCancel.setOnClickListener {
            onCancelClicked.invoke()
            isTermsAccepted  =false
            dismiss()
        }
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        isTermsAccepted = false
        onDismissListener?.invoke()
    }


    private fun updateToggleUI() {
        val iconRes = if (isTermsAccepted) {
            R.drawable.checkbox_checked_icon
        } else {
            R.drawable.checkbox_unchecked_circle
        }
        binding.ivCheckIcon.setImageResource(iconRes)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}