package com.craftrom.manager.core.utils

import android.view.animation.AlphaAnimation
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.craftrom.manager.R

object ToolbarTitleUtils {

    fun setToolbarText(activity: AppCompatActivity, title: String, subtitle: String?) {
        activity.supportActionBar?.apply {
            val titleTextView = activity.findViewById<TextView>(R.id.toolbar_title)
            val subtitleTextView = activity.findViewById<TextView>(R.id.toolbar_subtitle)

            // Clear any previous animations and reset text
            titleTextView.clearAnimation()
            subtitleTextView.clearAnimation()

            // Change title opacity
            animateTextView(titleTextView)
            titleTextView.text = title
            titleTextView.maxLines = 1
            animateTextView(subtitleTextView)
            subtitleTextView.text = subtitle
            subtitleTextView.maxLines = 1

        }
    }

    private fun animateTextView(textView: TextView) {
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 280 // Animation duration in milliseconds
        textView.startAnimation(anim)
    }
}



