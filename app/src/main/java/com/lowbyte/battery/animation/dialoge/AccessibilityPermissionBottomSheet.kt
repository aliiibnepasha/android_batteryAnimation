package com.lowbyte.battery.animation.dialoge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.AccessibilityPermissionDialogBinding

class AccessibilityPermissionBottomSheet(
    private val onAllowClicked: () -> Unit,
    private val onCancelClicked: () -> Unit
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

        binding.btnAllow.setOnClickListener {
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
            dismiss()
        }
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