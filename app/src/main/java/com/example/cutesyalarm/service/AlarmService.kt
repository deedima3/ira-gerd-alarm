package com.example.cutesyalarm.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.example.cutesyalarm.AlarmRingingActivity
import com.example.cutesyalarm.R
import com.example.cutesyalarm.receiver.AlarmReceiver

class AlarmService : Service() {

    companion object {
        const val CHANNEL_ID = "cutesy_alarm_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START_ALARM = "com.example.cutesyalarm.START_ALARM"
        const val ACTION_STOP_ALARM = "com.example.cutesyalarm.STOP_ALARM"
        
        fun startAlarm(context: Context, alarmId: Int, title: String, time: String) {
            val intent = Intent(context, AlarmService::class.java).apply {
                action = ACTION_START_ALARM
                putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
                putExtra(AlarmReceiver.EXTRA_ALARM_TITLE, title)
                putExtra(AlarmReceiver.EXTRA_ALARM_TIME, time)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopAlarm(context: Context) {
            val intent = Intent(context, AlarmService::class.java).apply {
                action = ACTION_STOP_ALARM
            }
            context.startService(intent)
        }
    }

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var alarmTitle: String = "Alarm"
    private var alarmTime: String = "00:00"
    private var alarmId: Int = 0

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_ALARM -> {
                alarmId = intent.getIntExtra(AlarmReceiver.EXTRA_ALARM_ID, 0)
                alarmTitle = intent.getStringExtra(AlarmReceiver.EXTRA_ALARM_TITLE) ?: "Alarm"
                alarmTime = intent.getStringExtra(AlarmReceiver.EXTRA_ALARM_TIME) ?: "00:00"
                startAlarm()
            }
            ACTION_STOP_ALARM -> {
                stopAlarm()
            }
        }
        return START_STICKY
    }

    private fun startAlarm() {
        // Acquire wake lock to ensure alarm works when phone is locked
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // API 33+: Use a simpler wakelock without ACQUIRE_CAUSES_WAKEUP (deprecated)
            // The notification with USE_FULL_SCREEN_INTENT will handle waking the device
            powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "CutesyAlarm::AlarmWakeLock"
            )
        } else {
            @Suppress("DEPRECATION")
            powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "CutesyAlarm::AlarmWakeLock"
            )
        }
        wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes

        // Play alarm sound
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        
        ringtone = RingtoneManager.getRingtone(this, alarmUri)
        ringtone?.play()

        // Vibrate
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator?.let { vib ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 500, 200, 500, 200, 500),
                        0
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(longArrayOf(0, 500, 200, 500, 200, 500), 0)
            }
        }

        // Start foreground service with notification (fullScreenIntent will launch activity when locked)
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun stopAlarm() {
        ringtone?.stop()
        vibrator?.cancel()
        wakeLock?.release()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun createNotification(): Notification {
        val dismissIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, AlarmService::class.java).apply {
                action = ACTION_STOP_ALARM
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Full screen intent to wake up device and show alarm activity
        val fullScreenIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmReceiver.EXTRA_ALARM_TITLE, alarmTitle)
            putExtra(AlarmReceiver.EXTRA_ALARM_TIME, alarmTime)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                    Intent.FLAG_ACTIVITY_NO_HISTORY
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Content intent for when user taps the notification body
        val contentIntent = PendingIntent.getActivity(
            this,
            alarmId + 1000,
            Intent(this, AlarmRingingActivity::class.java).apply {
                putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
                putExtra(AlarmReceiver.EXTRA_ALARM_TITLE, alarmTitle)
                putExtra(AlarmReceiver.EXTRA_ALARM_TIME, alarmTime)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⏰ $alarmTitle")
            .setContentText("It's $alarmTime - Wake up!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(contentIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(0, "Dismiss", dismissIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Cutesy Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarm notifications"
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), null)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                enableVibration(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        ringtone?.stop()
        vibrator?.cancel()
        wakeLock?.release()
    }
}
