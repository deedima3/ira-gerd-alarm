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
        
        // Create wake lock with appropriate flags for the Android version
        val wakeLockFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // API 33+: ACQUIRE_CAUSES_WAKEUP is deprecated but still functional
            // ON_AFTER_RELEASE keeps screen on briefly after release
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE
        } else {
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE
        }
        
        val wakeLock = powerManager.newWakeLock(
            wakeLockFlags,
            "CutesyAlarm::AlarmReceiverWakeLock"
        )
        
        // Acquire for 30 seconds - enough for service to start and notification to show
        wakeLock.acquire(30000)
        
        // Start alarm service first - this creates the notification with fullScreenIntent
        // which is the most reliable way to show alarm UI on locked devices
        AlarmService.startAlarm(context, alarmId.hashCode(), alarmTitle, alarmTime)
        
        // Schedule next occurrence (repeat daily)
        AlarmScheduler.scheduleNextAlarm(context, alarmId, alarmTitle, alarmTime)
        
        // Launch activity directly for unlocked devices
        // The service's fullScreenIntent handles locked devices via notification
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            try {
                val activityIntent = Intent(context, AlarmRingingActivity::class.java).apply {
                    putExtra(EXTRA_ALARM_ID, alarmId)
                    putExtra(EXTRA_ALARM_TITLE, alarmTitle)
                    putExtra(EXTRA_ALARM_TIME, alarmTime)
                    // Use FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK for fresh activity
                    // FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS keeps it out of recent apps
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or
                            Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                            Intent.FLAG_ACTIVITY_NO_HISTORY
                }
                
                // Check if we should use ContextCompat for better compatibility
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    // Android 14+: Use startForegroundService if available, otherwise normal start
                    context.startActivity(activityIntent)
                } else {
                    context.startActivity(activityIntent)
                }
            } catch (e: Exception) {
                // If direct launch fails (e.g., device locked or background restriction),
                // the fullScreenIntent in the service notification will handle it
                // The user will see the notification and can tap it to open the alarm
            }
        }, 300) // 300ms delay - faster to ensure it appears quickly
        
        // Schedule wake lock release on a background thread
        // Use shorter delay since service now has its own wake lock
        Thread {
            Thread.sleep(10000) // 10 seconds is enough for service to be fully running
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