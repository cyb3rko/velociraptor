package com.pluscubed.velociraptor.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.parseAsHtml
import androidx.fragment.app.Fragment
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pluscubed.velociraptor.BuildConfig
import com.pluscubed.velociraptor.R
import com.pluscubed.velociraptor.databinding.ActivitySettingsBinding
import com.pluscubed.velociraptor.limit.LimitService
import com.pluscubed.velociraptor.utils.PrefUtils
import com.pluscubed.velociraptor.utils.Utils

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    companion object {
        const val PRIVACY_URL = "https://www.pluscubed.com/velociraptor/privacy_policy.html"
        const val TROUBLESHOOT_URL = "https://www.pluscubed.com/velociraptor/troubleshoot.html"
    }

    private val permissionsFragment = PermissionsFragment()
    private val providersFragment = ProvidersFragment()
    private val generalFragment = GeneralFragment()
    private val advancedFragment = AdvancedFragment()
    private var active: Fragment = permissionsFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
                .add(R.id.main_view, advancedFragment, "advanced").hide(advancedFragment).commit()
        supportFragmentManager.beginTransaction()
                .add(R.id.main_view, providersFragment, "providers").hide(providersFragment).commit()
        supportFragmentManager.beginTransaction()
                .add(R.id.main_view, generalFragment, "general").hide(generalFragment).commit()
        supportFragmentManager.beginTransaction()
                .add(R.id.main_view, permissionsFragment, "permissions").commit()


        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_advanced -> {
                    supportFragmentManager.beginTransaction().hide(active).show(advancedFragment)
                            .commit()
                    active = advancedFragment
                    true
                }
                R.id.action_general -> {
                    supportFragmentManager.beginTransaction().hide(active).show(generalFragment)
                            .commit()
                    active = generalFragment
                    true
                }
                R.id.action_providers -> {
                    supportFragmentManager.beginTransaction().hide(active).show(providersFragment)
                            .commit()
                    active = providersFragment
                    true
                }
                R.id.action_permissions -> {
                    supportFragmentManager.beginTransaction().hide(active).show(permissionsFragment)
                            .commit()
                    active = permissionsFragment
                    true
                }
                else -> {
                    false
                }
            }
        }

        if (BuildConfig.VERSION_CODE > PrefUtils.getVersionCode(this)
            && !PrefUtils.isFirstRun(this)
            && PrefUtils.isTermsAccepted(this)
        ) {
            showChangelog()
        }

        if (!PrefUtils.isTermsAccepted(this)) {
            showTermsDialog()
        }

        PrefUtils.setFirstRun(this, false)
        PrefUtils.setVersionCode(this, BuildConfig.VERSION_CODE)
    }

    override fun onPause() {
        super.onPause()
        startLimitService(false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings_about -> {
                showAboutDialog()
                return true
            }
            R.id.menu_settings_changelog -> {
                showChangelog()
                return true
            }
            R.id.menu_settings_support -> {
                //showSupportDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.about_dialog_title, BuildConfig.VERSION_NAME))
            .setMessage(getString(R.string.about_body).parseAsHtml())
            .setPositiveButton(R.string.terms) { _, _ -> showTermsDialog() }
            .setNegativeButton(R.string.licenses) { _, _ ->
                startActivity(Intent(this@SettingsActivity, OssLicensesMenuActivity::class.java))
            }
            .setIcon(R.mipmap.ic_launcher)
            .show()
    }

    private fun showTermsDialog() {
        var builder = MaterialAlertDialogBuilder(this)
            .setMessage(getString(R.string.terms_body).parseAsHtml())
            .setNeutralButton(R.string.privacy_policy, null)

        if (!PrefUtils.isTermsAccepted(this)) {
            builder = builder
                .setCancelable(false)
                .setPositiveButton(R.string.accept) { d, _ ->
                    PrefUtils.setTermsAccepted(this@SettingsActivity, true)
                    d.dismiss()
                }
        }
        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                Utils.openLink(this, binding.mainView, PRIVACY_URL)
            }
        }
        dialog.show()
    }

    private fun showChangelog() {
        ChangelogDialogFragment.newInstance().show(supportFragmentManager, "CHANGELOG_DIALOG")
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            invalidateStates()
        }
    }

    private fun invalidateStates() {
        val permissionGranted = Utils.isLocationPermissionGranted(this)
        val overlayEnabled = Settings.canDrawOverlays(this)
        if (permissionGranted && overlayEnabled) {
            startLimitService(true)
        }
    }

    private fun startLimitService(start: Boolean) {
        val intent = Intent(this, LimitService::class.java)
        if (!start) {
            intent.putExtra(LimitService.EXTRA_CLOSE, true)
        }
        try {
            startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
