package com.example.cutesyalarm.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import com.example.cutesyalarm.AlarmRingingActivity
import com.example.cutesyalarm.service.AlarmService
import com.example.cutesyalarm.util.AlarmScheduler
import java.time.LocalDateTime
import java.time.ZoneId

class AlarmReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra(EXTRA_ALARM_ID) ?: return
        val alarmTitle = intent.getStringExtra(EXTRA_ALARM_TITLE) ?: "Alarm"
        val alarmTime = intent.getStringExtra(EXTRA_ALARM_TIME) ?: "00:00"
        
        // Wake up the device - keep wake lock until service takes over
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        
        // Use ACQUIRE_CAUSES_WAKEUP to turn on the screen immediately
        val wakeLockFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // API 33+: ACQUIRE_CAUSES_WAKEUP is deprecated, but still works
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE
        } else {
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE
        }
        
        val wakeLock = powerManager.newWakeLock(
            wakeLockFlags,
            "CutesyAlarm::AlarmReceiverWakeLock"
        )
        wakeLock.acquire(60000) // 60 seconds - give service time to start and show UI
        
        // Start alarm service for reliable lock screen behavior
        AlarmService.startAlarm(context, alarmId.hashCode(), alarmTitle, alarmTime)
        
        // Schedule next occurrence (repeat daily)
        AlarmScheduler.scheduleNextAlarm(context, alarmId, alarmTitle, alarmTime)
        
        // Also try to launch activity directly for immediate feedback
        // This works when the device is not in deep doze
        try {
            val activityIntent = Intent(context, AlarmRingingActivity::class.java).apply {
                putExtra(EXTRA_ALARM_ID, alarmId)
                putExtra(EXTRA_ALARM_TITLE, alarmTitle)
                putExtra(EXTRA_ALARM_TIME, alarmTime)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            }
            context.startActivity(activityIntent)
        } catch (e: Exception) {
            // Activity might not launch if device is locked, fullScreenIntent in service will handle it
        }
        
        // Release wake lock after a delay to ensure service is running
        Thread {
            Thread.sleep(10000)
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }.start()
    }
    
    companion object {
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_ALARM_TITLE = "alarm_title"
        const val EXTRA_ALARM_TIME = "alarm_time"
        
        fun createPendingIntent(
            context: Context,
            alarmId: String,
            title: String,
            time: String
        ): PendingIntent {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = "com.example.cutesyalarm.ALARM_TRIGGERED"
                putExtra(EXTRA_ALARM_ID, alarmId)
                putExtra(EXTRA_ALARM_TITLE, title)
                putExtra(EXTRA_ALARM_TIME, time)
            }
            
            return PendingIntent.getBroadcast(
                context,
                alarmId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}