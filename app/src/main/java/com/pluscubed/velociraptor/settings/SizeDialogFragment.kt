package com.pluscubed.velociraptor.settings

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
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

        setup(true, binding.textPercentLimit, binding.edittextPercentLimit, binding.seekbarPercentLimit)
        setup(
                false,
                binding.textPercentSpeedometer,
                binding.edittextPercentSpeedometer,
                binding.seekbarPercentSpeedometer
        )

        return MaterialDialog(activity!!)
                .customView(view = binding.root, scrollable = true)
                .title(R.string.size)
                .negativeButton(android.R.string.cancel) {
                    setSize(true, initialLimitSize)
                    setSize(false, initialSpeedometerSize)
                    Utils.updateFloatingServicePrefs(activity)
                }
                .positiveButton(android.R.string.ok)
    }

    private fun setup(
            limit: Boolean,
            percentText: TextView,
            percentEditText: EditText,
            percentSeekbar: SeekBar
    ) {
        percentText.text = getString(R.string.percent, "")
        percentEditText.setText((getSize(limit) * 100).toInt().toString())
        percentEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                try {
                    val constant = Integer.parseInt(s.toString())
                    percentSeekbar.progress = constant - 50
                } catch (e: NumberFormatException) {
                    percentSeekbar.progress = 50
                }

                try {
                    setSize(
                            limit,
                            java.lang.Float.parseFloat(percentEditText.text.toString()) / 100f
                    )
                    Utils.updateFloatingServicePrefs(activity)
                } catch (ignored: NumberFormatException) {
                }

            }
        })
        percentSeekbar.progress = (getSize(limit) * 100).toInt() - 50
        percentSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    percentEditText.setText((progress + 50).toString())

                    try {
                        setSize(
                                limit,
                                java.lang.Float.parseFloat(percentEditText.text.toString()) / 100f
                        )
                        Utils.updateFloatingServicePrefs(activity)
                    } catch (ignored: NumberFormatException) {
                    }

                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
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