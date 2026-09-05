package com.example.cutesyalarm.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.cutesyalarm.service.AlarmService
import com.example.cutesyalarm.util.AlarmScheduler

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
        
        // Start alarm service - this creates the notification with fullScreenIntent
        // which is the proper way to show alarm UI. The fullScreenIntent handles
        // both locked and unlocked devices, preventing duplicate activity launches.
        AlarmService.startAlarm(context, alarmId.hashCode(), alarmTitle, alarmTime)
        
        // Schedule next occurrence (repeat daily)
        AlarmScheduler.scheduleNextAlarm(context, alarmId, alarmTitle, alarmTime)
        
        // Note: We do NOT launch the activity directly here.
        // The notification's fullScreenIntent (set in AlarmService) handles showing
        // the AlarmRingingActivity. This prevents the alarm from ringing twice.
        
        // Schedule wake lock release on a background thread
        // Use shorter delay since service now has its own wake lock
        Thread {
            Thread.sleep(5000) // 5 seconds is enough for service to be fully running
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
        
        /**
         * Check if the app can schedule exact alarms (Android 12+)
         * Returns true if permission is granted or not required (Android < 12)
         */
        fun canScheduleExactAlarms(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                return alarmManager.canScheduleExactAlarms()
            }
            return true // Not required on older Android versions
        }
        
        /**
         * Open the exact alarm permission settings screen for the user to grant permission
         */
        fun openExactAlarmPermissionSettings(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }
    }
}