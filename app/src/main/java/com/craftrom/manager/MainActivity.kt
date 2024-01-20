package com.craftrom.manager

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.craftrom.manager.core.utils.ToolbarTitleProvider
import com.craftrom.manager.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isRootFragment = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setSupportActionBar(binding.appBarMain.toolbar)
        val defaultFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)?.childFragmentManager?.fragments?.get(0)
        if (defaultFragment is ToolbarTitleProvider) {
            setToolbarText(defaultFragment.getTitle(), defaultFragment.getSubtitle())
        }
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_jitter, R.id.navigation_dcenter, R.id.navigation_device, R.id.navigation_about))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        navController.addOnDestinationChangedListener { _, destination, _ ->
            isRootFragment = when (destination.id) {
                R.id.navigation_home,
                R.id.navigation_jitter,
                R.id.navigation_dcenter-> true
                else -> false
            }

            setDisplayHomeAsUpEnabled(!isRootFragment)
            requestNavigationHidden(this, !isRootFragment)

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun setDisplayHomeAsUpEnabled(isEnabled: Boolean) {
        when {
            isEnabled -> binding.appBarMain.toolbar.setNavigationIcon(R.drawable.ic_back)
            else -> binding.appBarMain.toolbar.navigationIcon = null
        }
    }

    fun setToolbarText(title: String, subtitle: String) {
        supportActionBar?.apply {
            val titleTextView = findViewById<TextView>(R.id.toolbar_title)
            val subtitleTextView = findViewById<TextView>(R.id.toolbar_subtitle)


            titleTextView.text = title
            subtitleTextView.text = subtitle
            titleTextView.maxLines = 1
            subtitleTextView.maxLines = 1

        }


    }

    companion object {
        internal fun requestNavigationHidden(
            mainActivity: MainActivity,
            hide: Boolean = true, requiresAnimation: Boolean = true) {
            val bottomView = mainActivity.binding.navView
            if (requiresAnimation) {
                bottomView.isVisible = true
                bottomView.isHidden = hide
            } else {
                bottomView.isGone = hide
            }
        }
    }
}