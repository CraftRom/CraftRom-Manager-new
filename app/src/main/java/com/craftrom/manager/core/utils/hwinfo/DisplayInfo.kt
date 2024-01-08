package com.craftrom.manager.core.utils.hwinfo

import android.content.Context
import android.graphics.Point
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import com.craftrom.manager.core.app.ServiceContext.context
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt


open class DisplayInfo  {

    companion object {

        private val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        // Device Screen Size

        fun getScreenSize(): String {
            val dm = DisplayMetrics()
            wm.defaultDisplay.getMetrics(dm)
            val x = (dm.widthPixels / dm.xdpi).toDouble().pow(2.0)
            val y = (dm.heightPixels / dm.ydpi).toDouble().pow(2.0)

            val screenInches = sqrt(x + y)

            Log.d("Screen Inch", screenInches.toString())

            return ((screenInches * 100).roundToInt() / 100f).toString()
        }

        // Device Screen Resolution
        fun getScreenResolution(): String {
            val display = wm.defaultDisplay
            val size = Point()
            display.getRealSize(size)

            return size.x.toString() + "x" + size.y.toString()
        }

        fun getScreenDensity(): String {
            return (context.resources.displayMetrics.density * 160f).toInt().toString()
        }

        private fun errorResult() = "n/a"
    }
}