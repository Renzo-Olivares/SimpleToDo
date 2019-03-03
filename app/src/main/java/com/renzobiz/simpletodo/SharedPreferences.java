package com.renzobiz.simpletodo;

import android.content.Context;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatDelegate;

public class SharedPreferences {
    public static final String PREF_DARK_MODE = "darkMode";

    public static int getStoredPrefDarkMode(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_DARK_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public static void setPrefDarkMode(Context context, int preference){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(PREF_DARK_MODE, preference)
                .apply();
    }
}
