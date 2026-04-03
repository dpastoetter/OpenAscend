package com.openascend.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object ReminderChannels {
    const val MORNING = "openascend_morning"
    const val EVENING = "openascend_evening"
    const val BOSS = "openascend_boss"

    fun ensure(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        nm.createNotificationChannel(
            NotificationChannel(MORNING, "Morning overview", NotificationManager.IMPORTANCE_DEFAULT),
        )
        nm.createNotificationChannel(
            NotificationChannel(EVENING, "Evening check-in", NotificationManager.IMPORTANCE_DEFAULT),
        )
        nm.createNotificationChannel(
            NotificationChannel(BOSS, "Weekly boss", NotificationManager.IMPORTANCE_DEFAULT),
        )
    }
}
