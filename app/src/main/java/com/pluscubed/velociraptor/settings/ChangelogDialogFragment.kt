package com.pluscubed.velociraptor.settings

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pluscubed.velociraptor.R
import com.pluscubed.velociraptor.utils.getColorResCompat
import java.io.BufferedReader
import java.io.InputStreamReader

class ChangelogDialogFragment : DialogFragment() {
    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val customView = LayoutInflater.from(activity).inflate(R.layout.dialog_webview, null)
        val dialog = MaterialAlertDialogBuilder(requireActivity())
            .setView(customView)
            .setTitle(R.string.changelog)
            .setPositiveButton(android.R.string.ok, null)
            .setNeutralButton(R.string.rate) { _, _ ->
                Intent(Intent.ACTION_VIEW).let {
                    it.data = Uri.parse("https://play.google.com/store/apps/details?id=com.pluscubed.velociraptor")
                    startActivity(it)
                }
            }
            //.negativeButton(R.string.support) { (activity as SettingsActivity).showSupportDialog() }
            .create()

        val webView = customView.findViewById<View>(R.id.webview) as WebView
        try {
            // Load from changelog.html in the assets folder
            val buf = StringBuilder()
            val html = resources.openRawResource(R.raw.changelog)
            val reader = BufferedReader(InputStreamReader(html))
            reader.forEachLine { str ->
                buf.append(str)
            }
            reader.close()

            val primaryColor = activity?.getColorResCompat(android.R.attr.textColorPrimary)
                    ?: Color.BLACK

            val str = buf.toString().replace("TEXTCOLOR", String.format("#%06X", 0xFFFFFF and primaryColor))

            // Inject color values for WebView body background and links
            webView.loadDataWithBaseURL(null, str, "text/html", "UTF-8", null)

            webView.setBackgroundColor(Color.TRANSPARENT)
        } catch (e: Throwable) {
            webView.loadDataWithBaseURL(
                null,
                "<h1>Unable to load</h1><p>${e.localizedMessage}</p>",
                "text/html",
                "UTF-8",
                null
            )
        }

        return dialog
    }

    companion object {
        fun newInstance() = ChangelogDialogFragment()
    }
}
