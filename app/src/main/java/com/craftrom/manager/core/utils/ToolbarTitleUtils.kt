package com.craftrom.manager.core.utils

import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.craftrom.manager.R

object ToolbarTitleUtils {
    private var subtitleHandler: Handler? = null
    private var subtitleRunnable: Runnable? = null

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

            // Start typing animation for subtitle
            subtitleHandler?.removeCallbacks(subtitleRunnable!!)
            subtitleRunnable = Runnable {
                animateTextPrinting(subtitleTextView, subtitle ?: "")
            }
            subtitleHandler = Handler(Looper.getMainLooper())
            subtitleHandler?.post(subtitleRunnable!!)
        }
    }

    private fun animateTextPrinting(textView: TextView, text: String) {
        val textLength = text.length
        val delayMillis: Long = 50 // Delay between each character

        for (i in 0..textLength) {
            val displayedText = if (i < textLength) {
                text.substring(0, i) + "_"
            } else {
                text.substring(0, i)
            }
            textView.text = displayedText
            Thread.sleep(delayMillis)
        }
    }

    private fun animateTextView(textView: TextView) {
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 280 // Animation duration in milliseconds
        textView.startAnimation(anim)
    }
}



