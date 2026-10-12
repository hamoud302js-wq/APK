package com.example.studyplanner.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.Calendar;

/**
 * Reads and writes user preferences backed by {@link SharedPreferences}.
 */
public final class PrefsManager {

    public static final String KEY_NOTIFICATIONS = "pref_notifications_enabled";
    public static final String KEY_SOUND = "pref_sound_enabled";
    public static final String KEY_DEFAULT_REMINDER_MINUTES = "pref_default_reminder_minutes";
    public static final String KEY_DARK_MODE = "pref_dark_mode";
    public static final String KEY_USER_NAME = "pref_user_name";
    public static final String KEY_LOGGED_IN = "pref_logged_in";

    /** Default reminder lead time in minutes before the due time. */
    public static final int DEFAULT_REMINDER_MINUTES = 15;

    private static final String PREFS = "study_planner_prefs";

    private final SharedPreferences prefs;

    public PrefsManager(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean areNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
    }

    public boolean isSoundEnabled() {
        return prefs.getBoolean(KEY_SOUND, true);
    }

    public void setSoundEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply();
    }

    public int getDefaultReminderMinutes() {
        return prefs.getInt(KEY_DEFAULT_REMINDER_MINUTES, DEFAULT_REMINDER_MINUTES);
    }

    public void setDefaultReminderMinutes(int minutes) {
        prefs.edit().putInt(KEY_DEFAULT_REMINDER_MINUTES, minutes).apply();
    }

    public int getDarkMode() {
        return prefs.getInt(KEY_DARK_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void setDarkMode(int mode) {
        prefs.edit().putInt(KEY_DARK_MODE, mode).apply();
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public void setUserName(String name) {
        prefs.edit().putString(KEY_USER_NAME, name).apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public void setLoggedIn(boolean loggedIn) {
        prefs.edit().putBoolean(KEY_LOGGED_IN, loggedIn).apply();
    }

    /** Applies the stored dark mode selection to the whole application. */
    public void applyTheme() {
        AppCompatDelegate.setDefaultNightMode(getDarkMode());
    }

    /** Removes all stored preferences, used by the data reset option. */
    public void clear() {
        prefs.edit().clear().apply();
    }

    /** Returns today's start-of-day timestamp. */
    public static long startOfToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
