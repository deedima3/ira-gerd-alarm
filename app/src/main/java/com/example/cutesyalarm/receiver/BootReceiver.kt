package com.example.cutesyalarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.cutesyalarm.model.getDefaultAlarms
import com.example.cutesyalarm.util.AlarmScheduler

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule all default alarms after reboot
            val alarms = getDefaultAlarms()
            alarms.forEach { alarm ->
                AlarmScheduler.scheduleAlarm(
                    context = context,
                    alarmId = alarm.id,
                    title = alarm.title,
                    time = alarm.time,
                    isEnabled = alarm.isEnabled
                )
            }
        }
    }
}