package com.pluscubed.velociraptor.settings.appselection

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import com.pluscubed.velociraptor.BuildConfig
import java.util.*

object SelectedAppDatabase {
    /**
     * Returns sorted list of map apps (packageName, name)
     */
    fun getMapApps(context: Context): List<AppInfo> {
        return getMapAppsSync(context).sorted()
    }

    /**
     * Returns list of map apps (packageName, name)
     */
    private fun getMapAppsSync(context: Context): List<AppInfo> {
        val appInfos = ArrayList<AppInfo>()
        val gmmIntentUri = Uri.parse("geo:37.421999,-122.084056")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

        for (info in context.packageManager.queryIntentActivities(mapIntent)) {
            val appInfo = AppInfo()
            appInfo.packageName = info.activityInfo.packageName
            appInfo.name = info.loadLabel(context.packageManager).toString()
            appInfos.add(appInfo)
        }
        return appInfos
    }

    /**
     * Returns sorted list of AppInfos (packageName, name)
     */
    fun getInstalledApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        return pm.queryIntentActivities(mainIntent)
            .map {
                AppInfo().apply {
                    packageName = it.activityInfo.packageName
                    name = it.activityInfo.applicationInfo.loadLabel(pm).toString()
                }
            }
            .filter { it.packageName != BuildConfig.APPLICATION_ID }
            .sorted()
    }

    private fun PackageManager.queryIntentActivities(intent: Intent): List<ResolveInfo> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            this.queryIntentActivities(intent, 0)
        }
    }
}
