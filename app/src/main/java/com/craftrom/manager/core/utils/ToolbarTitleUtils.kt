package com.craftrom.manager.core.utils


import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.craftrom.manager.R

object ToolbarTitleUtils {
    fun setToolbarText(activity: AppCompatActivity, title: String, subtitle: String?) {
        activity.supportActionBar?.apply {
            val titleTextView = activity.findViewById<TextView>(R.id.toolbar_title)
            val subtitleTextView = activity.findViewById<TextView>(R.id.toolbar_subtitle)

            // Змінюємо прозорість заголовка
            animateTextView(titleTextView)
            titleTextView.text = title
            titleTextView.maxLines = 1

            // Застосовуємо ефект друкування до підзаголовка
            animateTextPrinting(subtitleTextView, subtitle ?: "")
            subtitleTextView.maxLines = 1
        }
    }

    private fun animateTextPrinting(textView: TextView, text: String) {
        val textLength = text.length
        val delayMillis: Long = 100 // Затримка між відображенням кожного символу

        val handler = Handler(Looper.getMainLooper())
        var currentLength = 0

        handler.postDelayed(object : Runnable {
            override fun run() {
                if (currentLength <= textLength) {
                    val displayedText = if (currentLength % 2 == 0 && currentLength < textLength) {
                        text.substring(0, currentLength) + "_"
                    } else {
                        text.substring(0, currentLength)
                    }
                    textView.text = displayedText
                    currentLength++
                    handler.postDelayed(this, delayMillis)
                }
            }
        }, delayMillis)
    }

    private fun animateTextView(textView: TextView) {
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 300 // Тривалість анімації у мілісекундах
        textView.startAnimation(anim)
    }
}



