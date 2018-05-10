package com.bradcypert.textico.services;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.bradcypert.textico.R;

public class ThemeService {

    /**
     * Sets the theme to a given activity theme
     * @param activity
     * @param theme the R.style... for the theme
     */
    public static void setTheme(Activity activity, int theme) {
        SharedPreferences sharedPref = activity.getSharedPreferences("theme_settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("theme", theme);
        editor.apply();
    }

    public static void setThemeNoActionBar(Activity activity, int theme) {
        SharedPreferences sharedPref = activity.getSharedPreferences("theme_settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("theme_no_action_bar", theme);
        editor.apply();
    }

    public static void setThemeName(Activity activity, String themeName) {
        SharedPreferences sharedPref = activity.getSharedPreferences("theme_settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("theme_name", themeName);
        editor.apply();
    }

    /**
     * Gets the current theme
     * @param activity
     * @param actionBar - Should the theme provide the action bar?
     * @return the R.style... for a theme
     */
    public static int getSelectedTheme(Activity activity, boolean actionBar) {
        SharedPreferences sharedPref = activity.getSharedPreferences("theme_settings", Context.MODE_PRIVATE);
        if (actionBar) {
            return sharedPref.getInt("theme", R.style.AppTheme);
        } else {
            return sharedPref.getInt("theme_no_action_bar", R.style.AppTheme_NoActionBar);
        }
    }

    public static String getThemeName(Activity activity) {
        SharedPreferences sharedPref = activity.getSharedPreferences("theme_settings", Context.MODE_PRIVATE);
        return sharedPref.getString("theme_name", "Modern");
    }
}
