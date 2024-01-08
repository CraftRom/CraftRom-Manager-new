package com.craftrom.manager.core.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.craftrom.manager.R
import com.craftrom.manager.core.app.ServiceContext.context
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

object Constants {
    const val TAG = "CraftRom"
    const val KERNEL_NAME = "Chidori"
    const val KALI_NAME = "Tsukuyoumi"

    // Інші константи
    const val SPLASH_TIME_OUT = 2000L // 2 sec
    const val INTERVAL_1_FPS = 1000L
    // News
    var DEFAULT_NEWS_SOURCE: String = "https://www.craft-rom.pp.ua/"
    var DEFAULT_NEWS_COUNT: Int = 15


    const val DATE_FORMAT_API = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    const val DATE_FORMAT_USER = "dd.MM.yyyy HH:mm"

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
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
