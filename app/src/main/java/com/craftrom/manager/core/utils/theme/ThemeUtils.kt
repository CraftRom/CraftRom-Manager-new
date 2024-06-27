package com.craftrom.manager.core.utils.theme

import androidx.appcompat.app.AppCompatDelegate

enum class ThemeType {
    DEFAULT_MODE,
    LIGHT_MODE,
    DARK_MODE
}

fun applyTheme(theme: ThemeType) {
    when (theme) {
        ThemeType.LIGHT_MODE -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        ThemeType.DARK_MODE -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        ThemeType.DEFAULT_MODE -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}
