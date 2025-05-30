package com.lowbyte.battery.animation.dialoge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lowbyte.battery.animation.databinding.AccessibilityPermissionDialogBinding

class AccessibilityPermissionBottomSheet(
    private val onAllowClicked: () -> Unit,
    private val onCancelClicked: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: AccessibilityPermissionDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AccessibilityPermissionDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Allow button click
        binding.btnAllow.setOnClickListener {
            if (binding.checkTerms.isChecked) {
                onAllowClicked.invoke()
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Please accept Terms of Service", Toast.LENGTH_SHORT).show()
            }
        }

        // Cancel button click
        binding.btnCancel.setOnClickListener {
            onCancelClicked.invoke()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}