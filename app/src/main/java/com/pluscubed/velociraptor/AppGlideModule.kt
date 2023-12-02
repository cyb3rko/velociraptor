package com.pluscubed.velociraptor

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.pluscubed.velociraptor.settings.appselection.AppInfo
import com.pluscubed.velociraptor.settings.appselection.AppInfoIconLoader
import java.io.InputStream

@GlideModule
internal class AppGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(
            AppInfo::class.java,
            InputStream::class.java,
            AppInfoIconLoader.AppInfoIconLoaderFactory(context)
        )
    }
}
