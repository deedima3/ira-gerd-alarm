package com.example.cutesyalarm

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class CutesyAlarmApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmChannel = NotificationChannel(
                CHANNEL_ID,
                "Cutesy Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Gentle reminders for medicine and meals"
                setSound(android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI, null)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(alarmChannel)
        }
    }
    
    companion object {
        const val CHANNEL_ID = "cutesy_alarm_channel"
    }
}