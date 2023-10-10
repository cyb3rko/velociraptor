package com.pluscubed.velociraptor.settings

import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.pluscubed.velociraptor.R
import com.pluscubed.velociraptor.databinding.DialogSizeBinding
import com.pluscubed.velociraptor.utils.PrefUtils
import com.pluscubed.velociraptor.utils.Utils

class SizeDialogFragment : DialogFragment() {
    private var _binding: DialogSizeBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private var initialLimitSize: Float = 0.toFloat()
    private var initialSpeedometerSize: Float = 0.toFloat()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogSizeBinding.inflate(layoutInflater)

        initialLimitSize = getSize(true)
        initialSpeedometerSize = getSize(false)

        setup(
            true,
            binding.textPercentLimit,
            binding.sliderPercentLimit
        )
        setup(
            false,
            binding.textPercentSpeedometer,
            binding.sliderPercentSpeedometer
        )

        return MaterialAlertDialogBuilder(requireActivity())
            .setView(binding.root)
            .setTitle(R.string.size)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                setSize(true, initialLimitSize)
                setSize(false, initialSpeedometerSize)
                Utils.updateFloatingServicePrefs(activity)
            }
            .create()
    }

    private fun setup(
        limit: Boolean,
        percentText: TextView,
        percentSlider: Slider
    ) {
        val initialValue = getSize(limit) * 100
        percentText.text = getString(R.string.percent, initialValue.toInt().toString())
        percentSlider.value = initialValue
        percentSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                percentText.text = getString(R.string.percent, value.toInt().toString())
            }
        }
        percentSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                try {
                    setSize(limit, percentSlider.value / 100f)
                    Utils.updateFloatingServicePrefs(activity)
                } catch (ignored: NumberFormatException) {
                }
            }
        })
    }

    private fun getSize(limit: Boolean): Float {
        return if (limit) PrefUtils.getSpeedLimitSize(activity) else PrefUtils.getSpeedometerSize(
                activity
        )
    }

    private fun setSize(limit: Boolean, size: Float) {
        if (limit) {
            PrefUtils.setSpeedLimitSize(activity, size)
        } else {
            PrefUtils.setSpeedometerSize(activity, size)
        }
    }

}