package com.pluscubed.velociraptor.settings.appselection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import com.pluscubed.velociraptor.R
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

internal class AppInfoIconLoader(private val context: Context) : ModelLoader<AppInfo, InputStream> {
    private var cancelled = false

    fun Drawable.drawableToStream(): InputStream? {
        val bitmap: Bitmap
        if (this is BitmapDrawable) {
            bitmap = this.bitmap
        } else {
            val width: Int = context.resources.getDimensionPixelSize(R.dimen.icon_size)
            bitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
        }
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream)
        val imageInByte = stream.toByteArray()
        val inputStream: InputStream = ByteArrayInputStream(imageInByte)
        return if (cancelled) null else inputStream
    }

    override fun buildLoadData(
        model: AppInfo,
        width: Int,
        height: Int,
        options: Options
    ): LoadData<InputStream> {
        return LoadData(ObjectKey(model.packageName), object : DataFetcher<InputStream> {
            override fun loadData(
                priority: Priority,
                callback: DataFetcher.DataCallback<in InputStream>
            ) {
                cancelled = false
                val pm = context.packageManager
                val stream = pm.getApplicationInfo(model.packageName, 0)
                    .loadIcon(pm)
                    .drawableToStream()
                callback.onDataReady(stream)
            }

            override fun cleanup() {}

            override fun cancel() {}

            override fun getDataClass(): Class<InputStream> {
                return InputStream::class.java
            }

            override fun getDataSource(): DataSource {
                return DataSource.LOCAL
            }
        })
    }

    override fun handles(model: AppInfo): Boolean {
        return true
    }

    class AppInfoIconLoaderFactory(
        private val context: Context
    ) : ModelLoaderFactory<AppInfo, InputStream> {
        override fun build(
            multiFactory: MultiModelLoaderFactory
        ): ModelLoader<AppInfo, InputStream> {
            return AppInfoIconLoader(context)
        }

        override fun teardown() {}
    }
}
