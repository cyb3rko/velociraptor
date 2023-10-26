package com.pluscubed.velociraptor.settings

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.pluscubed.velociraptor.R
import com.pluscubed.velociraptor.databinding.DialogOpacityBinding
import com.pluscubed.velociraptor.utils.PrefUtils
import com.pluscubed.velociraptor.utils.Utils

class OpacityDialogFragment : DialogFragment() {
    private var _binding: DialogOpacityBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private var initialTransparency: Int = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogOpacityBinding.inflate(layoutInflater)

        initialTransparency = PrefUtils.getOpacity(activity)

        binding.textPercent.text = getString(R.string.percent, initialTransparency.toString())
        val percentSlider = binding.sliderPercent
        percentSlider.value = PrefUtils.getOpacity(activity).toFloat()
        percentSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                binding.textPercent.text = getString(R.string.percent, value.toInt().toString())
            }
        }
        percentSlider.addOnSliderTouchListener(object: Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                try {
                    PrefUtils.setOpacity(activity, slider.value.toInt())
                    Utils.updateFloatingServicePrefs(activity)
                } catch (ignored: NumberFormatException) {}
            }
        })
        return MaterialAlertDialogBuilder(requireActivity())
            .setView(binding.root)
            .setTitle(R.string.transparency)
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                PrefUtils.setOpacity(activity, initialTransparency)
                Utils.updateFloatingServicePrefs(activity)
            }
            .setPositiveButton(android.R.string.ok, null)
            .create()
    }
}
