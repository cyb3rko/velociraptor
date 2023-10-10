package com.pluscubed.velociraptor.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.text.parseAsHtml
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pluscubed.velociraptor.R
import com.pluscubed.velociraptor.databinding.FragmentProvidersBinding
import com.pluscubed.velociraptor.utils.Utils
import kotlinx.coroutines.*
import java.util.*

class ProvidersFragment : Fragment() {
    private var _binding: FragmentProvidersBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProvidersBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.osmCoverage.setOnClickListener {
            openOsmCoverage()
        }

        binding.osmEditdata.setOnClickListener {
            val message = getString(R.string.osm_edit).replace("%s", "<b>$OSM_EDITDATA_URL</b>")
            MaterialAlertDialogBuilder(requireActivity())
                .setMessage(message.parseAsHtml())
                .setPositiveButton(R.string.share_link) { _, _ ->
                    Intent().let {
                        it.type = "text/plain"
                        it.putExtra(Intent.EXTRA_TEXT, OSM_EDITDATA_URL)
                        startActivity(
                            Intent.createChooser(it, getString(R.string.share_link))
                        )
                    }
                }
                .show()
        }

        binding.osmDonate.setOnClickListener { Utils.openLink(activity, view, OSM_DONATE_URL) }

        val checkIcon =
                activity?.let { AppCompatResources.getDrawable(it, R.drawable.ic_done_green_20dp) }
        binding.osmTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, checkIcon, null)
    }

    @SuppressLint("MissingPermission")
    private fun openOsmCoverage() {
        if (Utils.isLocationPermissionGranted(activity)) {
            activity?.let {
                val fusedLocationProvider = LocationServices.getFusedLocationProviderClient(it)
                fusedLocationProvider.lastLocation.addOnCompleteListener(it) { task ->
                    var uriString = OSM_COVERAGE_URL
                    if (task.isSuccessful && task.result != null) {
                        val lastLocation = task.result
                        uriString +=
                                "?lon=${lastLocation?.longitude}&lat=${lastLocation?.latitude}&zoom=12"
                    }
                    Utils.openLink(activity, view, uriString)
                }
            }
        } else {
            Utils.openLink(activity, view, OSM_COVERAGE_URL)
        }
    }

    companion object {
        const val OSM_EDITDATA_URL = "https://openstreetmap.org"
        const val OSM_COVERAGE_URL = "https://product.itoworld.com/map/124"
        const val OSM_DONATE_URL = "https://donate.openstreetmap.org"
    }
}