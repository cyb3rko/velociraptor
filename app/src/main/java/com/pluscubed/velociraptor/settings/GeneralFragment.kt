package com.pluscubed.velociraptor.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.R as R2
import androidx.fragment.app.Fragment
import com.pluscubed.velociraptor.R
import com.pluscubed.velociraptor.databinding.FragmentGeneralBinding
import com.pluscubed.velociraptor.utils.PrefUtils
import com.pluscubed.velociraptor.utils.Utils

class GeneralFragment : Fragment() {
    private var _binding: FragmentGeneralBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeneralBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        invalidateStates()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val unitAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item_text, arrayOf("mph", "km/h"))
        unitAdapter.setDropDownViewResource(R2.layout.support_simple_spinner_dropdown_item)
        val unitSpinner = binding.spinnerUnit
        unitSpinner.adapter = unitAdapter
        unitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                val isMetric = position == 1
                if (PrefUtils.getUseMetric(activity) != isMetric) {
                    PrefUtils.setUseMetric(activity, isMetric)
                    unitSpinner.dropDownVerticalOffset = Utils.convertDpToPx(
                            activity,
                            (unitSpinner.selectedItemPosition * -48).toFloat()
                    )

                    Utils.updateFloatingServicePrefs(activity)
                    invalidateStates()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        unitSpinner.setSelection(if (PrefUtils.getUseMetric(activity)) 1 else 0)
        unitSpinner.dropDownVerticalOffset =
                Utils.convertDpToPx(activity, (unitSpinner.selectedItemPosition * -48).toFloat())

        val styleAdapter = ArrayAdapter(
                requireContext(),
                R.layout.spinner_item_text,
                arrayOf(getString(R.string.united_states), getString(R.string.international))
        )
        styleAdapter.setDropDownViewResource(R2.layout.support_simple_spinner_dropdown_item)
        val styleSpinner = binding.spinnerStyle
        styleSpinner.adapter = styleAdapter
        styleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                if (position != PrefUtils.getSignStyle(activity)) {
                    PrefUtils.setSignStyle(activity, position)
                    styleSpinner.dropDownVerticalOffset =
                            Utils.convertDpToPx(
                                    activity,
                                    (styleSpinner.selectedItemPosition * -48).toFloat()
                            )

                    Utils.updateFloatingServicePrefs(activity)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        styleSpinner.setSelection(PrefUtils.getSignStyle(activity))
        styleSpinner.dropDownVerticalOffset =
                Utils.convertDpToPx(activity, (styleSpinner.selectedItemPosition * -48).toFloat())

        binding.linearTolerance.setOnClickListener { v ->
            ToleranceDialogFragment().show(
                    childFragmentManager,
                    "dialog_tolerance"
            )
        }

        binding.linearSize.setOnClickListener { v ->
            SizeDialogFragment().show(
                    childFragmentManager,
                    "dialog_size"
            )
        }

        binding.linearOpacity.setOnClickListener { v ->
            OpacityDialogFragment().show(
                    childFragmentManager,
                    "dialog_opacity"
            )
        }

        val showSpeedometerSwitch = binding.switchSpeedometer
        showSpeedometerSwitch.isChecked = PrefUtils.getShowSpeedometer(activity)
        (showSpeedometerSwitch.parent as View).setOnClickListener { v ->
            showSpeedometerSwitch.isChecked = !showSpeedometerSwitch.isChecked

            PrefUtils.setShowSpeedometer(activity, showSpeedometerSwitch.isChecked)

            Utils.updateFloatingServicePrefs(activity)
        }

        val showSpeedLimitsSwitch = binding.switchLimits
        showSpeedLimitsSwitch.isChecked = PrefUtils.getShowLimits(activity)
        (showSpeedLimitsSwitch.parent as View).setOnClickListener { v ->
            showSpeedLimitsSwitch.isChecked = !showSpeedLimitsSwitch.isChecked

            PrefUtils.setShowLimits(activity, showSpeedLimitsSwitch.isChecked)

            Utils.updateFloatingServicePrefs(activity)
        }

        val beepSwitch = binding.switchBeep
        beepSwitch.isChecked = PrefUtils.isBeepAlertEnabled(activity)
        beepSwitch.setOnClickListener { v ->
            PrefUtils.setBeepAlertEnabled(
                    activity,
                    beepSwitch.isChecked
            )
        }
        binding.buttonTestBeep.setOnClickListener { v -> Utils.playBeeps() }
    }

    private fun invalidateStates() {
        val constant = getString(
                if (PrefUtils.getUseMetric(activity)) R.string.kmph else R.string.mph,
                PrefUtils.getSpeedingConstant(activity).toString()
        )
        val percent = getString(R.string.percent, PrefUtils.getSpeedingPercent(activity).toString())
        val mode = if (PrefUtils.getToleranceMode(activity)) "+" else getString(R.string.or)
        val overview = getString(R.string.tolerance_desc, percent, mode, constant)
        binding.textOverviewTolerance.text = overview

        val limitSizePercent = getString(
                R.string.percent,
                (PrefUtils.getSpeedLimitSize(activity) * 100).toInt().toString()
        )
        val speedLimitSize = getString(R.string.size_limit_overview, limitSizePercent)
        val speedometerSizePercent = getString(
                R.string.percent,
                (PrefUtils.getSpeedometerSize(activity) * 100).toInt().toString()
        )
        val speedometerSize = getString(R.string.size_speedometer_overview, speedometerSizePercent)
        binding.textOverviewSize.text = speedLimitSize + "\n" + speedometerSize

        binding.textOverviewOpacity.text =
                getString(R.string.percent, PrefUtils.getOpacity(activity).toString())
    }
}