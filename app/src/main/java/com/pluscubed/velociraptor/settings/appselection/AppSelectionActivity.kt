package com.pluscubed.velociraptor.settings.appselection

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pluscubed.velociraptor.R
import com.pluscubed.velociraptor.databinding.ActivityAppselectionBinding
import com.pluscubed.velociraptor.detection.AppDetectionService
import com.pluscubed.velociraptor.utils.PrefUtils
import kotlinx.coroutines.*
import java.util.*

class AppSelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppselectionBinding
    private lateinit var adapter: AppAdapter

    private var selectedPackageNames: MutableSet<String>? = null
    private var allApps: ArrayList<AppInfo>? = null
    private var mapApps: ArrayList<AppInfo>? = null

    private var isMapsOnly: Boolean = false

    private var isLoadingAllApps: Boolean = false
    private var isLoadingMapApps: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppselectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = AppAdapter(
            applicationContext,
            isSelected = {
                if (selectedPackageNames != null) {
                    selectedPackageNames!!.contains(it)
                } else {
                    false
                }
            },
            onItemClick = { appInfo, checked ->
                onItemClick(appInfo, checked)
            }
        )
        binding.recyclerview.let {
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(this)
        }

        binding.swiperefresh.setOnRefreshListener {
            if (isMapsOnly) {
                reloadMapApps()
            } else {
                reloadInstalledApps()
            }
            adapter.submitList(ArrayList())
        }

        if (savedInstanceState == null) {
            isMapsOnly = true
        } else {
            allApps = savedInstanceState.getParcelableArrayList(STATE_APPS)
            mapApps = savedInstanceState.getParcelableArrayList(STATE_MAP_APPS)
            isMapsOnly = savedInstanceState.getBoolean(STATE_MAPS_ONLY)
            selectedPackageNames = savedInstanceState.getStringArrayList(STATE_SELECTED_APPS)?.let {
                HashSet(it)
            }
        }

        if (mapApps == null) {
            reloadMapApps()
        } else if (isMapsOnly) {
            adapter.submitList(mapApps)
        }

        if (allApps == null) {
            reloadInstalledApps()
        } else if (!isMapsOnly) {
            adapter.submitList(allApps)
        }

        setTitle(R.string.select_apps)
    }

    override fun onPostResume() {
        super.onPostResume()
        if (isLoadingAllApps || isLoadingMapApps) {
            binding.swiperefresh.isRefreshing = true
        }
    }

    private fun reloadInstalledApps() = lifecycleScope.launch {
        isLoadingAllApps = true
        if (!isMapsOnly) {
            binding.swiperefresh.isRefreshing = true
        }
        selectedPackageNames = HashSet(PrefUtils.getApps(this@AppSelectionActivity))

        try {
            val installedApps = withContext(Dispatchers.IO) {
                SelectedAppDatabase.getInstalledApps(this@AppSelectionActivity)
            }

            if (!isMapsOnly) {
                adapter.submitList(installedApps)
                binding.swiperefresh.isRefreshing = false
            }
            allApps = ArrayList(installedApps)

            isLoadingAllApps = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun reloadMapApps() = lifecycleScope.launch {
        isLoadingMapApps = true
        if (isMapsOnly) {
            binding.swiperefresh.isRefreshing = true
        }
        selectedPackageNames = PrefUtils.getApps(this@AppSelectionActivity)

        try {
            val mapApps = withContext(Dispatchers.IO) {
                SelectedAppDatabase.getMapApps(this@AppSelectionActivity)
            }

            if (isMapsOnly) {
                adapter.submitList(mapApps)
                binding.swiperefresh.isRefreshing = false
            }
            this@AppSelectionActivity.mapApps = ArrayList(mapApps)

            isLoadingMapApps = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_app_selection, menu)
        val item = menu.findItem(R.id.menu_app_selection_maps)
        var drawable = AppCompatResources.getDrawable(this, R.drawable.ic_map_white_24dp)!!.mutate()
        drawable = DrawableCompat.wrap(drawable)
        if (isMapsOnly) {
            DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.colorPrimaryA200))
        }
        item.icon = drawable
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_app_selection_done -> {
                finish()
                return true
            }
            R.id.menu_app_selection_maps -> {
                isMapsOnly = !isMapsOnly
                invalidateOptionsMenu()
                adapter.submitList(if (isMapsOnly) mapApps else allApps)
                binding.swiperefresh.isRefreshing = isMapsOnly && isLoadingMapApps ||
                        !isMapsOnly && isLoadingAllApps
                return true
            }
        }
        return false
    }

    private fun onItemClick(appInfo: AppInfo, checked: Boolean) {
        if (appInfo.packageName != null && appInfo.packageName.isNotEmpty()) {
            if (checked) {
                selectedPackageNames?.add(appInfo.packageName)
            } else {
                selectedPackageNames?.remove(appInfo.packageName)
            }

            PrefUtils.setApps(this, selectedPackageNames)
            if (AppDetectionService.get() != null) {
                AppDetectionService.get().updateSelectedApps()
            }
        }

        val allMapApps = mapApps?.let { mapApps ->
            (selectedPackageNames?.containsAll(mapApps.map { it.packageName }) == true)
        } ?: false
        PrefUtils.setAllMapApps(this@AppSelectionActivity, allMapApps)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(STATE_APPS, allApps)
        outState.putParcelableArrayList(STATE_MAP_APPS, mapApps)
        outState.putBoolean(STATE_MAPS_ONLY, isMapsOnly)
        outState.putStringArrayList(STATE_SELECTED_APPS, ArrayList(selectedPackageNames!!))
        super.onSaveInstanceState(outState)
    }

    companion object {
        const val STATE_SELECTED_APPS = "state_selected_apps"
        const val STATE_APPS = "state_apps"
        const val STATE_MAP_APPS = "state_map_apps"
        const val STATE_MAPS_ONLY = "state_maps_only"
    }
}
