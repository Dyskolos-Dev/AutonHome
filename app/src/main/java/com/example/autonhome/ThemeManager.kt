package com.example.autonhome

import android.content.Context

object ThemeManager {
    private const val PREFS_NAME = "ThemePrefs"
    private const val DARK_THEME_KEY = "isDarkTheme"

    fun saveThemePreference(context: Context, isDarkTheme: Boolean) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(DARK_THEME_KEY, isDarkTheme).apply()
    }

    fun loadThemePreference(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(DARK_THEME_KEY, false)
    }
}

