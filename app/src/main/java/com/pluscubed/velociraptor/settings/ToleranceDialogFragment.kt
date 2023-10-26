package com.pluscubed.velociraptor.settings

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pluscubed.velociraptor.R
import com.pluscubed.velociraptor.databinding.DialogToleranceBinding
import com.pluscubed.velociraptor.utils.PrefUtils
import com.pluscubed.velociraptor.utils.Utils

class ToleranceDialogFragment : DialogFragment() {
    private var _binding: DialogToleranceBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogToleranceBinding.inflate(layoutInflater)

        val constantSlider = binding.sliderConstant
        val percentSlider = binding.sliderPercent
        val andButton = binding.buttonAnd
        val orButton = binding.buttonOr

        val initialConstant = PrefUtils.getSpeedingConstant(activity)
        binding.textConstantUnit.text =  Utils.getUnitText(
            requireActivity(),
            initialConstant.toString()
        )
        constantSlider.value = initialConstant.toFloat()
        constantSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                binding.textConstantUnit.text = Utils.getUnitText(
                    requireActivity(),
                    value.toInt().toString()
                )
            }
        }

        val initialPercent = PrefUtils.getSpeedingPercent(activity)
        binding.textPercent.text = getString(R.string.percent, initialPercent.toString())
        percentSlider.value = initialPercent.toFloat()
        percentSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                binding.textPercent.text = getString(R.string.percent, value.toInt().toString())
            }
        }

        andButton.isChecked = PrefUtils.getToleranceMode(activity)
        orButton.isChecked = !PrefUtils.getToleranceMode(activity)
        andButton.setOnClickListener {
            andButton.isChecked = true
            orButton.isChecked = false
        }
        orButton.setOnClickListener {
            orButton.isChecked = true
            andButton.isChecked = false
        }

        return MaterialAlertDialogBuilder(requireActivity())
            .setView(binding.root)
            .setTitle(R.string.speeding_amount)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                try {
                    PrefUtils.setSpeedingConstant(
                        activity,
                        constantSlider.value.toInt()
                    )
                    PrefUtils.setSpeedingPercent(
                        activity,
                        percentSlider.value.toInt()
                    )
                } catch (ignored: NumberFormatException) {}

                PrefUtils.setToleranceMode(activity, andButton.isChecked)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Utils.updateFloatingServicePrefs(activity)
    }
}
