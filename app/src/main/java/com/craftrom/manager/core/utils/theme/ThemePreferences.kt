package com.craftrom.manager.core.utils.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.craftrom.manager.R
import com.craftrom.manager.core.app.ServiceContext.context

class ThemePreferences(context: Context) {

    private val preferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    fun getSelectedTheme(): ThemeType {
        val themeValue = preferences.getString(
            context.getString(R.string.settings_dark_theme_key),
            ThemeType.DEFAULT_MODE.name
        ) ?: ThemeType.DEFAULT_MODE.name

        return try {
            ThemeType.valueOf(themeValue)
        } catch (e: IllegalArgumentException) {
            ThemeType.DEFAULT_MODE
        }
    }

    fun setSelectedTheme(theme: ThemeType) {
        preferences.edit().putString(
            context.getString(R.string.settings_dark_theme_key),
            theme.name
        ).apply()
    }
}

