package com.craftrom.manager.ui.activity

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.PackageInfoCompat
import com.craftrom.manager.MainActivity
import com.craftrom.manager.R
import com.craftrom.manager.core.utils.Constants
import java.util.*

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var imageLogo: ImageView
    private lateinit var textViewVersion: TextView
    private lateinit var textCopirate: TextView

    private val currentYear: String
        get() = Calendar.getInstance().get(Calendar.YEAR).toString()

    private val scaleAnimation by lazy {
        ScaleAnimation(
            0.65f,
            1f,
            0.65f,
            1f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        ).apply {
            duration = Constants.SPLASH_TIME_OUT
            fillAfter = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Налаштування вікна для розтягнутого вигляду під статус-бар та виріз
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        val params = window.attributes
        params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        window.attributes = params
        setContentView(R.layout.activity_splash)

        val firstSharedPreferences = getSharedPreferences("SysPrefs", MODE_PRIVATE)
        if (!firstSharedPreferences.contains("firstRun")) {
            val editor = firstSharedPreferences.edit()
            editor.putBoolean("firstRun", true)
            editor.apply()
        }


        initView()
    }

    private fun initView() {
        imageLogo = findViewById(R.id.imageLogo)
        textViewVersion = findViewById(R.id.textViewVersion)
        textCopirate = findViewById(R.id.text)

        val appName = resources.getString(R.string.app_name)
        textCopirate.text = resources.getString(R.string.copyr, currentYear)

        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                packageManager.getPackageInfo(packageName, 0)
            }
            val versionName = packageInfo.versionName
            val versionNumber = PackageInfoCompat.getLongVersionCode(packageInfo).toString()
            textViewVersion.text = resources.getString(R.string.app_name_version, appName, versionName)
        } catch (_: PackageManager.NameNotFoundException) {
        }

        scaleAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                val firstSharedPreferences = getSharedPreferences("SysPrefs", MODE_PRIVATE)
                val isFirstRun = firstSharedPreferences.getBoolean("firstRun", true)

                if (isFirstRun) {
                    // Перший запуск
                    Constants.changeActivity<IntroActivity>(this@SplashActivity)
                    finish()
                } else {
                    // Не перший запуск
                    Constants.changeActivity<MainActivity>(this@SplashActivity)
                    finish()
                }
            }

            override fun onAnimationRepeat(animation: Animation?) {
                // TODO: Implement
            }
        })

        imageLogo.startAnimation(scaleAnimation)
    }
}

