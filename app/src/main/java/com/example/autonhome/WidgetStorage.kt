package com.example.autonhome

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder

object WidgetStorage {
    private const val PREFS_NAME = "WidgetPrefs"
    private const val WIDGETS_KEY = "widgets"

    private fun getSharedPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveWidgets(context: Context, widgets: List<WidgetData>) {
        try {
            val gson = GsonBuilder().create()
            val json = gson.toJson(widgets)
            getSharedPrefs(context).edit().putString(WIDGETS_KEY, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadWidgets(context: Context): List<WidgetData> {
        return try {
            val gson = GsonBuilder().create()
            val json = getSharedPrefs(context).getString(WIDGETS_KEY, null)
            if (json != null) {
                gson.fromJson(json, Array<WidgetData>::class.java).toList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

