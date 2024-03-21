package com.craftrom.manager.core.utils

import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.craftrom.manager.R

object ToolbarTitleUtils {
    fun setToolbarText(activity: AppCompatActivity, title: String, subtitle: String) {
        activity.supportActionBar?.apply {
            val titleTextView = activity.findViewById<TextView>(R.id.toolbar_title)
            val subtitleTextView = activity.findViewById<TextView>(R.id.toolbar_subtitle)

            titleTextView.text = title
            subtitleTextView.text = subtitle
            titleTextView.maxLines = 1
            subtitleTextView.maxLines = 1
        }
    }
}
