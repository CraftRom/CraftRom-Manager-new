/*
 *  Copyright (C) 2020 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.craftrom.manager.ui.activity

import android.app.ActivityManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.provider.Settings
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.craftrom.manager.R
import com.craftrom.manager.core.utils.theme.ThemePreferences
import com.craftrom.manager.core.utils.theme.ThemeType
import com.craftrom.manager.core.utils.theme.applyTheme
import com.craftrom.manager.databinding.SettingsActivityBinding


class SettingsActivity : AppCompatActivity() {

    private lateinit var mBinding: SettingsActivityBinding

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preference, rootKey)

            val notificationPreference =
                findPreference<Preference>("notification_settings")
            notificationPreference?.setOnPreferenceClickListener {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireActivity().packageName)
                startActivity(intent)
                false
            }

            val themePreferences = ThemePreferences(requireContext())
            findPreference<ListPreference>(getString(R.string.settings_dark_theme_key))?.setOnPreferenceChangeListener { _, newValue ->
                val themeType = when (newValue) {
                    "1" -> ThemeType.LIGHT_MODE
                    "2" -> ThemeType.DARK_MODE
                    else -> ThemeType.DEFAULT_MODE
                }
                themePreferences.setSelectedTheme(themeType)
                applyTheme(themeType)
                true
            }
        }

        override fun onStop() {
            super.onStop()
            val themePreferences = ThemePreferences(requireContext())
            applyTheme(themePreferences.getSelectedTheme())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val td = ActivityManager.TaskDescription.Builder()
                .setPrimaryColor(getAttrColor(android.R.attr.colorBackground)).build()
            setTaskDescription(td)
        }

        mBinding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setSupportActionBar(mBinding.toolbar)

        this.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setToolbarText(getString(R.string.settings), getSubtitle())
    }
    private fun getSubtitle() = " "
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportFragmentManager.beginTransaction().replace(R.id.content, SettingsFragment())
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setToolbarText(title: String, subtitle: String) {
        supportActionBar?.apply {
            val titleTextView = findViewById<TextView>(R.id.toolbar_title)
            val subtitleTextView = findViewById<TextView>(R.id.toolbar_subtitle)

            titleTextView.text = title
            subtitleTextView.text = subtitle
            titleTextView.maxLines = 1
            subtitleTextView.maxLines = 1

        }
    }

    private fun getAttrColor(attr: Int): Int {
        val ta = obtainStyledAttributes(intArrayOf(attr))
        val color = ta.getColor(0, 0)
        ta.recycle()
        return color
    }

}
