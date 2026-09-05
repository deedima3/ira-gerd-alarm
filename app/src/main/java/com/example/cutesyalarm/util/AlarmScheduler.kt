package com.example.cutesyalarm.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import com.example.cutesyalarm.util.AlarmPreferences
import com.example.cutesyalarm.receiver.AlarmReceiver
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object AlarmScheduler {
    
    fun scheduleAlarm(
        context: Context,
        alarmId: String,
        title: String,
        time: LocalTime,
        isEnabled: Boolean
    ) {
        if (!isEnabled) return
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Calculate next occurrence
        val now = LocalDateTime.now()
        var alarmDateTime = LocalDateTime.of(now.toLocalDate(), time)
        
        // If time has passed today, schedule for tomorrow
        if (alarmDateTime.isBefore(now)) {
            alarmDateTime = alarmDateTime.plusDays(1)
        }
        
        val triggerTimeMillis = alarmDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val pendingIntent = AlarmReceiver.createPendingIntent(
            context, alarmId, title, time.toString()
        )
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (AlarmReceiver.canScheduleExactAlarms(context)) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                } else {
                    // Fallback to inexact alarm if permission not granted
                    // Use setAlarmClock for better visibility in status bar (shows next alarm)
                    alarmManager.setAlarmClock(
                        AlarmManager.AlarmClockInfo(triggerTimeMillis, null),
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback to inexact alarm if permission denied
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerTimeMillis, null),
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
        }
    }
    
    fun scheduleNextAlarm(
        context: Context,
        alarmId: String,
        title: String,
        timeString: String
    ) {
        val time = LocalTime.parse(timeString)
        scheduleAlarm(context, alarmId, title, time, true)
    }
    
    fun cancelAlarm(context: Context, alarmId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent: PendingIntent = AlarmReceiver.createPendingIntent(
            context, alarmId, "", ""
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
    
    /**
     * Reschedules all alarms. First cancels any existing alarms with the given IDs,
     * then schedules new ones. This prevents duplicate alarms when the app restarts.
     */
    fun rescheduleAllAlarms(
        context: Context,
        alarms: List<com.example.cutesyalarm.model.Alarm>
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        alarms.forEach { alarm ->
            // First cancel any existing alarm with this ID to prevent duplicates
            val pendingIntent: PendingIntent = AlarmReceiver.createPendingIntent(
                context, alarm.id, "", ""
            )
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            
            // Then schedule the alarm if it's enabled
            val isEnabled = AlarmPreferences.isAlarmEnabled(context, alarm.id, alarm.isEnabled)
            if (isEnabled) {
                scheduleAlarm(context, alarm.id, alarm.title, alarm.time, true)
            }
        }
    }
    
    /**
     * Reschedules alarms from SharedPreferences (for BootReceiver to restore user alarms)
     */
    fun rescheduleAllAlarmsFromPrefs(context: Context) {
        val alarms = com.example.cutesyalarm.model.getDefaultAlarms()
        rescheduleAllAlarms(context, alarms)
    }
    
    // Schedule an alarm at a specific time in milliseconds (for testing)
    fun scheduleAlarmAtTime(
        context: Context,
        alarmId: String,
        title: String,
        triggerTimeMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val pendingIntent = AlarmReceiver.createPendingIntent(
            context, alarmId, title, "TEST"
        )
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (AlarmReceiver.canScheduleExactAlarms(context)) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAlarmClock(
                        AlarmManager.AlarmClockInfo(triggerTimeMillis, null),
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback to inexact alarm if permission denied
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerTimeMillis, null),
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
        }
    }
}