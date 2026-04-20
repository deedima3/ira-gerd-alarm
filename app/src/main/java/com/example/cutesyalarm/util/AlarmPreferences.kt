package com.example.cutesyalarm.util

import android.content.Context
import android.content.SharedPreferences

object AlarmPreferences {
    private const val PREFS_NAME = "alarm_prefs"
    private const val KEY_PREFIX = "alarm_enabled_"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun isAlarmEnabled(context: Context, alarmId: String, defaultValue: Boolean = true): Boolean {
        return getPrefs(context).getBoolean(KEY_PREFIX + alarmId, defaultValue)
    }
    
    fun setAlarmEnabled(context: Context, alarmId: String, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_PREFIX + alarmId, enabled).apply()
    }
    
    fun clearAll(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
