package com.craftrom.manager.core.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.craftrom.manager.R
import com.craftrom.manager.core.app.ServiceContext.context
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

object Constants {
    const val TAG = "CraftRom"

    // Інші константи
    const val SPLASH_TIME_OUT = 2000L // 2 sec
    const val INTERVAL_1_FPS = 1000L
    // News
    var DEFAULT_NEWS_SOURCE: String = "https://www.craft-rom.pp.ua/"

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

        if (connectivityManager == null) {
            Log.e("InternetCheck", "ConnectivityManager is null")
            return false
        }

        try {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
                !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                Log.i("InternetCheck", "No internet capability or network not validated")
                return false
            }

            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("InternetCheck", "Connected via WiFi or Ethernet")
                return true
            }

            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                val minBandwidthKbps = 500 // Minimum bandwidth threshold for cellular in Kbps
                val downstreamBandwidth = capabilities.linkDownstreamBandwidthKbps
                if (downstreamBandwidth >= minBandwidthKbps) {
                    Log.i("InternetCheck", "Connected via Cellular with sufficient bandwidth")
                    return true
                } else {
                    Log.i("InternetCheck", "Cellular bandwidth too low: $downstreamBandwidth Kbps")
                }
            }
        } catch (e: Exception) {
            Log.e("InternetCheck", "Exception during internet check", e)
        }

        return false
    }


    fun getMarkdownAsHtml(text: String): CharSequence? {
        return try {
            val parser = Parser.builder().build()
            val renderer = HtmlRenderer.builder().build()
            val document = parser.parse(text)
            val html = renderer.render(document)
            android.text.Html.fromHtml(html, android.text.Html.FROM_HTML_MODE_LEGACY)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    inline fun <reified T : Activity> changeActivity(activity: Activity) {
        val intent = Intent(activity, T::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(intent)
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        activity.finish()
    }
    inline fun <reified T : Activity> changeActivityRight(activity: Activity) {
        val intent = Intent(activity, T::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(intent)
        activity.overridePendingTransition(R.anim.left_in, R.anim.right_out)
        activity.finish()
    }
    inline fun <reified T : Activity> changeActivityLeft(activity: Activity) {
        val intent = Intent(activity, T::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(intent)
        activity.overridePendingTransition(R.anim.right_in, R.anim.left_out)
        activity.finish()
    }
    fun getYesNoString(value: Boolean) = if (value) {
        context.getString(R.string.yes)
    } else {
        context.getString(R.string.no)
    }
}
