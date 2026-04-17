package com.focuson.app.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class SessionAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val modeId = intent.getStringExtra(EXTRA_MODE_ID) ?: return
        val duration = intent.getIntExtra(EXTRA_DURATION, 60)
        val strict = intent.getBooleanExtra(EXTRA_STRICT, false)
        val svc = BlockSessionService.startIntent(context, modeId, duration, strict)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(svc)
        } else {
            context.startService(svc)
        }
    }

    companion object {
        const val ACTION_TRIGGER = "com.focuson.app.action.SCHEDULED_START"
        const val EXTRA_MODE_ID = "mode_id"
        const val EXTRA_DURATION = "duration_min"
        const val EXTRA_STRICT = "strict"

        fun scheduleExact(
            context: Context,
            triggerAtMillis: Long,
            requestCode: Int,
            modeId: String,
            durationMin: Int,
            strict: Boolean,
        ) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, SessionAlarmReceiver::class.java).apply {
                action = ACTION_TRIGGER
                putExtra(EXTRA_MODE_ID, modeId)
                putExtra(EXTRA_DURATION, durationMin)
                putExtra(EXTRA_STRICT, strict)
            }
            val pi = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            }
        }
    }
}
