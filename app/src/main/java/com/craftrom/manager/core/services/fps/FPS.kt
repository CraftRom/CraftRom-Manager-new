package com.craftrom.manager.core.services.fps

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.craftrom.manager.R
import com.craftrom.manager.core.time.CoroutineTimer
import com.craftrom.manager.core.utils.Constants
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import kotlin.math.roundToInt

class FPS : Service() {
    private lateinit var tvFps: TextView
    private lateinit var layoutView: View
    private lateinit var windowManager: WindowManager
    private var previousTime: Long = 0
    private var frameCount = 0
    private var fps = 0

    private val intervalometer = CoroutineTimer {
        calculateFPS()
    }
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        // Attach View To Left Top Corner
        val inflater: LayoutInflater = LayoutInflater.from(this)
        layoutView = inflater.inflate(R.layout.layout_overlay, null)

        tvFps = layoutView.findViewById(R.id.tv_fps)

        val params: WindowManager.LayoutParams =
            WindowManager.LayoutParams(-2, -2, 2038, 4980792, -3)
        params.gravity = Gravity.START or Gravity.TOP

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(layoutView, params)

        // Keep Alive The Service
        val notificationChannel = NotificationChannel(
            "Craft_stats_notification_channel",
            "Craft_stats_notification_channel",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.setSound(null, null)
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            notificationChannel
        )
        val notificationBuilder = Notification.Builder(this, "Craft_stats_notification_channel")
        notificationBuilder.setContentTitle("Craft Rom FPS Meter")
            .setContentText("Keep FPS meter running...").setSmallIcon(R.drawable.ic_logo_splash)
        startForeground(69, notificationBuilder.build())
        onConfigurationChanged(resources.configuration)

        intervalometer.interval(Constants.INTERVAL_1_FPS)
    }


    private fun calculateFPS() {
        val dir = findFpsFilePath()
        try {
            // Спробуємо отримати дані через RandomAccessFile
            val fps = RandomAccessFile(dir, "r").use { it.readLine().toInt() }
            this@FPS.tvFps.text = fps.toString()
            Log.e(Constants.TAG, "FPS : $fps")
        } catch (ioe: IOException) {
            // Обробка помилки читання через RandomAccessFile
            try {
                val fps = RandomAccessFile(dir, "r").use { it.readLine().split(" ")[1].toInt() }
                this@FPS.tvFps.text = fps.toDouble().roundToInt().toString()
                Log.e(Constants.TAG, "FPS : $fps")
            } catch (shellException: Exception) {
                // Обробка помилки
                intervalometer.stop()
                Log.e(Constants.TAG, "Cannot read FPS $dir", ioe)
            }
        } catch (nfe: NumberFormatException) {
            // Обробка невірно форматованих даних
            intervalometer.stop()
            Log.e(Constants.TAG, "Read a badly formatted FPS $dir", nfe)
        }

    }

    private fun findFpsFilePath(): String {
        val sysFolder = File("/sys")

        val measuredFps = sysFolder.walk()
            .maxDepth(3)
            .filter { it.name == "measured_fps" }
            .map { it.absolutePath }
            .firstOrNull()

        val fpsFilePath = if (measuredFps != null && measuredFps.isNotEmpty()) {
            measuredFps
        } else {
            sysFolder.walk()
                .maxDepth(3)
                .filter { it.name == "fps" }
                .map { it.absolutePath }
                .firstOrNull() ?: ""
        }

        Log.e(Constants.TAG, "FPS file found: $fpsFilePath")
        return fpsFilePath
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            tvFps.setPadding(
                resources.getDimension(R.dimen.l1).toInt(),
                resources.getDimension(R.dimen.l1).toInt(),
                0,
                0
            )
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            tvFps.setPadding(resources.getDimension(R.dimen.l1).toInt(), 0, 0, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        intervalometer.stop()
        windowManager.removeView(layoutView)
    }
}
