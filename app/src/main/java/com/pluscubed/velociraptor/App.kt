package com.pluscubed.velociraptor

import android.app.Application
import com.pluscubed.velociraptor.settings.appselection.SelectedAppDatabase
import com.pluscubed.velociraptor.utils.PrefUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        if (!PrefUtils.isFirstRun(this)) return
        GlobalScope.launch {
            try {
                val mapApps = withContext(Dispatchers.IO) {
                    SelectedAppDatabase.getMapApps(this@App)
                }.filter { appInfoEntity ->
                    appInfoEntity.packageName != null && appInfoEntity.packageName.isNotEmpty()
                }.map { appInfo -> appInfo.packageName }

                PrefUtils.setApps(this@App, mapApps.toHashSet())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
