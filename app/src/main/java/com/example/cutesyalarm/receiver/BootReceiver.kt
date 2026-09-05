package com.example.cutesyalarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.cutesyalarm.util.AlarmScheduler

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule all alarms after reboot
            // rescheduleAllAlarmsFromPrefs cancels any existing alarms first, then reschedules
            // This prevents duplicate alarms after a reboot
            AlarmScheduler.rescheduleAllAlarmsFromPrefs(context)
        }
    }
}