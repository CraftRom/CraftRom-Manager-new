package com.craftrom.manager

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.craftrom.manager.core.utils.ToolbarTitleUtils.setToolbarText
import com.craftrom.manager.core.utils.interfaces.ToolbarTitleProvider
import com.craftrom.manager.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isRootFragment = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_home, R.id.navigation_jitter, R.id.navigation_dcenter, R.id.navigation_device, R.id.navigation_about, R.id.navigation_settings))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            isRootFragment = when (destination.id) {
                R.id.navigation_home,
                R.id.navigation_jitter,
                R.id.navigation_dcenter -> true
                else -> false
            }

            setDisplayHomeAsUpEnabled(!isRootFragment)
            requestNavigationHidden(this, !isRootFragment)

            binding.appBarMain.toolbar.post {
                updateToolbarText(navController.currentDestination)
            }
        }

    }

    private fun updateToolbarText(destination: NavDestination?) {
        val fragments = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)?.childFragmentManager?.fragments
        val currentFragment = fragments?.firstOrNull()
        if (currentFragment is ToolbarTitleProvider) {
            setToolbarText(this, currentFragment.getTitle(), currentFragment.getSubtitle())
        } else {
            setToolbarText(this, destination!!.label.toString(), null)
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
