package com.pluscubed.velociraptor.settings

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.pluscubed.velociraptor.BuildConfig
import com.pluscubed.velociraptor.R
import com.pluscubed.velociraptor.api.cache.CacheLimitProvider
import com.pluscubed.velociraptor.databinding.FragmentAdvancedBinding
import com.pluscubed.velociraptor.limit.LimitService
import com.pluscubed.velociraptor.settings.appselection.AppSelectionActivity
import com.pluscubed.velociraptor.utils.NotificationUtils
import com.pluscubed.velociraptor.utils.PrefUtils
import com.pluscubed.velociraptor.utils.Utils
import kotlinx.coroutines.*

class AdvancedFragment : Fragment() {
    private var _binding: FragmentAdvancedBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var cacheLimitProvider: CacheLimitProvider

    private var notificationManager: NotificationManager? = null

    private val isNotificationAccessGranted: Boolean
        get() = context?.let {
            NotificationManagerCompat.getEnabledListenerPackages(it)
                    .contains(BuildConfig.APPLICATION_ID)
        } ?: false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        cacheLimitProvider = CacheLimitProvider(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdvancedBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationManager =
                activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        binding.switchNotifControls.setOnClickListener {
            val intent = Intent(context, LimitService::class.java)
            intent.putExtra(LimitService.EXTRA_NOTIF_START, true)
            val pending = PendingIntent.getService(
                context,
                PENDING_SERVICE,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val intentClose = Intent(context, LimitService::class.java)
            intentClose.putExtra(LimitService.EXTRA_NOTIF_CLOSE, true)
            val pendingClose = PendingIntent.getService(
                context,
                PENDING_SERVICE_CLOSE,
                intentClose,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val settings = Intent(context, SettingsActivity::class.java)
            val settingsIntent = PendingIntent.getActivity(
                context,
                PENDING_SETTINGS,
                settings,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            NotificationUtils.initChannels(context)
            val builder = context?.let {
                NotificationCompat.Builder(it, NotificationUtils.CHANNEL_TOGGLES)
                    .setSmallIcon(R.drawable.ic_speedometer_notif)
                    .setContentTitle(getString(R.string.controls_notif_title))
                    .setContentText(getString(R.string.controls_notif_desc))
                    .addAction(0, getString(R.string.show), pending)
                    .addAction(0, getString(R.string.hide), pendingClose)
                    .setDeleteIntent(pendingClose)
                    .setContentIntent(settingsIntent)
            }
            val notification = builder?.build()
            notificationManager?.notify(NOTIFICATION_CONTROLS, notification)
        }

        binding.linearClearCache.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) { cacheLimitProvider.clear() }
                    Snackbar.make(
                        it,
                        getString(R.string.cache_cleared),
                        Snackbar.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Snackbar.make(it, "Error: $e", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        binding.buttonAppSelection.setOnClickListener {
            startActivity(Intent(context, AppSelectionActivity::class.java))
        }

        val debuggingSwitch = binding.switchDebugging
        debuggingSwitch.isChecked = PrefUtils.isDebuggingEnabled(context)
        (debuggingSwitch.parent as View).setOnClickListener { v ->
            debuggingSwitch.isChecked = !debuggingSwitch.isChecked

            PrefUtils.setDebugging(context, debuggingSwitch.isChecked)

            Utils.updateFloatingServicePrefs(context)
        }

        val gmapsOnlyNavigationSwitch = binding.switchGmapsNavigation
        gmapsOnlyNavigationSwitch.isChecked = isNotificationAccessGranted &&
                PrefUtils.isGmapsOnlyInNavigation(context)
        binding.linearGmapsNavigation.setOnClickListener {
            if (!gmapsOnlyNavigationSwitch.isEnabled) {
                return@setOnClickListener
            }

            val accessGranted = isNotificationAccessGranted
            if (accessGranted) {
                gmapsOnlyNavigationSwitch.toggle()
                PrefUtils.setGmapsOnlyInNavigation(
                    context,
                    gmapsOnlyNavigationSwitch.isChecked
                )
            } else {
                if (context == null) return@setOnClickListener
                MaterialAlertDialogBuilder(requireContext())
                    .setMessage(R.string.gmaps_only_nav_notif_access)
                    .setPositiveButton(R.string.grant) { _, _ ->
                        try {
                            val settingsAction = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
                            val intent = Intent(settingsAction)
                            startActivity(intent)
                        } catch (ignored: ActivityNotFoundException) {
                            Snackbar.make(
                                view,
                                R.string.open_settings_failed_notif_access,
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                    .show()
            }
        }
    }

    companion object {
        const val PENDING_SERVICE = 4
        const val PENDING_SERVICE_CLOSE = 3
        const val PENDING_SETTINGS = 2
        const val NOTIFICATION_CONTROLS = 42
    }
}
