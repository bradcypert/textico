package com.bradcypert.textico.services

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

import com.bradcypert.textico.R

object ThemeService {

    /**
     * Sets the theme to a given activity theme
     * @param activity
     * @param theme the R.style... for the theme
     */
    fun setTheme(activity: Activity, theme: Int) {
        val sharedPref = activity.getSharedPreferences("theme_settings", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt("theme", theme)
        editor.apply()
    }

    fun setThemeNoActionBar(activity: Activity, theme: Int) {
        val sharedPref = activity.getSharedPreferences("theme_settings", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt("theme_no_action_bar", theme)
        editor.apply()
    }

    fun setThemeName(activity: Activity, themeName: String) {
        val sharedPref = activity.getSharedPreferences("theme_settings", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("theme_name", themeName)
        editor.apply()
    }

    /**
     * Gets the current theme
     * @param activity
     * @param actionBar - Should the theme provide the action bar?
     * @return the R.style... for a theme
     */
    fun getSelectedTheme(activity: Activity, actionBar: Boolean): Int {
        val sharedPref = activity.getSharedPreferences("theme_settings", Context.MODE_PRIVATE)
        return if (actionBar) {
            sharedPref.getInt("theme", R.style.AppTheme)
        } else {
            sharedPref.getInt("theme_no_action_bar", R.style.AppTheme_NoActionBar)
        }
    }

    fun getThemeName(activity: Activity): String {
        val sharedPref = activity.getSharedPreferences("theme_settings", Context.MODE_PRIVATE)
        return sharedPref.getString("theme_name", "Modern")
    }
}
