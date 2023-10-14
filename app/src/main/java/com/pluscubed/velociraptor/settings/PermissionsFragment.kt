package com.pluscubed.velociraptor.settings

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.livinglifetechway.quickpermissionskotlin.runWithPermissions
import com.pluscubed.velociraptor.BuildConfig
import com.pluscubed.velociraptor.R
import com.pluscubed.velociraptor.databinding.FragmentPermissionsBinding
import com.pluscubed.velociraptor.detection.AppDetectionService
import com.pluscubed.velociraptor.utils.Utils
import timber.log.Timber

class PermissionsFragment : Fragment() {
    private var _binding: FragmentPermissionsBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPermissionsBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            binding.buttonEnableNotifications.setOnClickListener {
                runWithPermissions(Manifest.permission.POST_NOTIFICATIONS) {
                    Timber.d("Notification permission granted")
                }
            }
            binding.notificationsLayout.visibility = View.VISIBLE
            binding.buttonEnableNotifications.visibility = View.VISIBLE
        }

        binding.buttonTroubleshoot.setOnClickListener {
            Utils.openLink(activity, view, SettingsActivity.TROUBLESHOOT_URL)
        }

        binding.buttonEnableService.setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } catch (e: ActivityNotFoundException) {
                Snackbar.make(
                    it,
                    R.string.open_settings_failed_accessibility,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        binding.buttonFloatingEnabled.setOnClickListener {
            try {
                //Open the current default browswer App Info page
                openSettings(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, BuildConfig.APPLICATION_ID)
            } catch (ignored: ActivityNotFoundException) {
                Snackbar.make(
                    it,
                    R.string.open_settings_failed_overlay,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        binding.buttonLocationEnabled.setOnClickListener {
            runWithPermissions(Manifest.permission.ACCESS_FINE_LOCATION) {
                Timber.d("Notification permission granted")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        invalidateStates()
    }

    private fun invalidateStates() {
        val permissionGranted = Utils.isLocationPermissionGranted(activity)
        binding.imageLocationEnabled.setImageResource(
            if (permissionGranted) {
                R.drawable.ic_done_green_40dp
            } else {
                R.drawable.ic_cross_red_40dp
            }
        )
        binding.buttonLocationEnabled.isEnabled = !permissionGranted

        val overlayEnabled = Settings.canDrawOverlays(activity)
        binding.imageFloatingEnabled.setImageResource(
            if (overlayEnabled) {
                R.drawable.ic_done_green_40dp
            } else {
                R.drawable.ic_cross_red_40dp
            }
        )
        binding.buttonFloatingEnabled.isEnabled = !overlayEnabled

        val serviceEnabled =
                Utils.isAccessibilityServiceEnabled(activity, AppDetectionService::class.java)
        binding.imageServiceEnabled.setImageResource(if (serviceEnabled) R.drawable.ic_done_green_40dp else R.drawable.ic_cross_red_40dp)
        binding.buttonEnableService.isEnabled = !serviceEnabled

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val notificationsAllowed = Utils.isNotificationPermissionGranted(activity)
            binding.imageNotificationsEnabled.setImageResource(
                if (notificationsAllowed) {
                    R.drawable.ic_done_green_40dp
                } else {
                    R.drawable.ic_cross_red_40dp
                }
            )
            binding.buttonEnableNotifications.isEnabled = !notificationsAllowed
        }
    }

    private fun openSettings(settingsAction: String, packageName: String) {
        val intent = Intent(settingsAction)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }
}