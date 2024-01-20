package com.craftrom.manager.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.craftrom.manager.MainActivity
import com.craftrom.manager.core.utils.Constants
import com.craftrom.manager.databinding.ActivityIntroBinding
import com.squareup.picasso.BuildConfig


class IntroActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityIntroBinding

    private val getPermissions =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.ok.setOnClickListener {
            val firstSharedPreferences = getSharedPreferences("SysPrefs", MODE_PRIVATE)
            val editor = firstSharedPreferences.edit()
            editor.putBoolean("firstRun", false)
            editor.apply()
            Constants.changeActivity<MainActivity>(this@IntroActivity)
            finish()
        }
    }

    private fun hasInstallPermissions(): Boolean {
        return packageManager.canRequestPackageInstalls()
    }

    private fun checkUnknownResourceInstallation() {
        getPermissions.launch(
            Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${BuildConfig.APPLICATION_ID}")
            )
        )
    }
}
