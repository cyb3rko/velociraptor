package com.pluscubed.velociraptor.settings

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.SeekBar
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
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

        val constantEditText = binding.edittextConstant
        val constantSeekbar = binding.seekbarConstant
        val percentEditText = binding.edittextPercent
        val percentSeekbar = binding.seekbarPercent
        val andButton = binding.buttonAnd
        val orButton = binding.buttonOr

        binding.textConstantUnit.text = Utils.getUnitText(activity!!)
        constantEditText.setText(PrefUtils.getSpeedingConstant(activity).toString())
        constantEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                try {
                    val constant = Integer.parseInt(s.toString())
                    constantSeekbar.progress = constant + 25
                } catch (e: NumberFormatException) {
                    constantSeekbar.progress = 25
                }

            }
        })
        constantSeekbar.progress = PrefUtils.getSpeedingConstant(activity) + 25
        constantSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    constantEditText.setText((progress - 25).toString())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.textPercent.text = getString(R.string.percent, "")
        percentEditText.setText(PrefUtils.getSpeedingPercent(activity).toString())
        percentEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                try {
                    val constant = Integer.parseInt(s.toString())
                    percentSeekbar.progress = constant + 25
                } catch (e: NumberFormatException) {
                    percentSeekbar.progress = 25
                }

            }
        })
        percentSeekbar.progress = PrefUtils.getSpeedingPercent(activity) + 25
        percentSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    percentEditText.setText((progress - 25).toString())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

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

        return MaterialDialog(activity!!)
                .customView(view = binding.root, scrollable = true)
                .title(R.string.speeding_amount)
                .negativeButton(android.R.string.cancel)
                .positiveButton(android.R.string.ok) {
                    try {
                        PrefUtils.setSpeedingConstant(
                                activity,
                                Integer.parseInt(constantEditText.text.toString())
                        )
                        PrefUtils.setSpeedingPercent(
                                activity,
                                Integer.parseInt(percentEditText.text.toString())
                        )
                    } catch (ignored: NumberFormatException) {
                    }

                    PrefUtils.setToleranceMode(activity, andButton.isChecked)
                }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        Utils.updateFloatingServicePrefs(activity)
    }
}