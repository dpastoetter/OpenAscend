package com.openascend.app.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.openascend.app.MainActivity
import com.openascend.app.R
import com.openascend.app.util.weekStartMondayEpochDay
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        ReminderChannels.ensure(applicationContext)
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            ReminderWorkerEntryPoint::class.java,
        )
        val privacyPrefs = entryPoint.privacyPreferences()
        val privacy = privacyPrefs.getSettingsSnapshot()
        if (!privacy.remindersEnabled) return Result.success()

        val prefs = applicationContext.getSharedPreferences("openascend_reminders", Context.MODE_PRIVATE)
        val today = LocalDate.now().toEpochDay()
        val hour = LocalTime.now().hour

        val nm = NotificationManagerCompat.from(applicationContext)
        if (!nm.areNotificationsEnabled()) return Result.success()

        val profile = entryPoint.profileRepository().getProfile()
        val homeSnap = privacyPrefs.homeSnapshot.first()
        val weekStart = weekStartMondayEpochDay()
        val bossSealedThisWeek = homeSnap.bossRitualSealedWeekStart == weekStart

        val weakestStatName = run {
            val day = today
            val habits = entryPoint.habitRepository().observeHabits().first()
            val rollingMetrics = entryPoint.metricsRepository().metricsBetween(day - 6, day)
            val completionMap = mutableMapOf<Pair<Long, Long>, Boolean>()
            for (offset in 0L..6L) {
                val d = day - offset
                for (h in habits) {
                    completionMap[h.id to d] =
                        entryPoint.habitRepository().isCompleted(h.id, d)
                }
            }
            val rolling = entryPoint.statComputation().computeRollingSevenDay(
                lastSevenDays = rollingMetrics,
                habits = habits,
                isHabitCompleted = { hid, epoch -> completionMap[Pair(hid, epoch)] == true },
                todayEpochDay = day,
            )
            rolling.weakestStat().name.replaceFirstChar { it.titlecase() }
        }

        if (privacy.reminderMorningEnabled && hour == 8) {
            val key = "morning_$today"
            if (prefs.getBoolean(key, false)) return Result.success()
            prefs.edit().putBoolean(key, true).apply()
            showNotification(
                ReminderChannels.MORNING,
                applicationContext.getString(R.string.notify_morning_title),
                applicationContext.getString(R.string.notify_morning_body),
                "openascend://home",
            )
        }
        if (privacy.reminderEveningEnabled && hour == 20) {
            if (profile?.lastLoggedEpochDay == today) return Result.success()
            val key = "evening_$today"
            if (prefs.getBoolean(key, false)) return Result.success()
            prefs.edit().putBoolean(key, true).apply()
            val body = applicationContext.getString(
                R.string.notify_evening_body_weak,
                weakestStatName,
            )
            showNotification(
                ReminderChannels.EVENING,
                applicationContext.getString(R.string.notify_evening_title),
                body,
                "openascend://check_in",
            )
        }
        if (privacy.reminderBossEnabled && hour == 10 && LocalDate.now().dayOfWeek.value == 1) {
            if (bossSealedThisWeek) return Result.success()
            val key = "boss_week_${LocalDate.now().year}_${LocalDate.now().dayOfYear}"
            if (prefs.getBoolean(key, false)) return Result.success()
            prefs.edit().putBoolean(key, true).apply()
            val body = applicationContext.getString(
                R.string.notify_boss_body_weak,
                weakestStatName,
            )
            showNotification(
                ReminderChannels.BOSS,
                applicationContext.getString(R.string.notify_boss_title),
                body,
                "openascend://boss",
            )
        }
        return Result.success()
    }

    private fun showNotification(channelId: String, title: String, text: String, deepLink: String) {
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(deepLink)).apply {
            setClass(applicationContext, MainActivity::class.java)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            applicationContext,
            channelId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notif = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        if (!canPostNotifications(applicationContext)) return
        try {
            NotificationManagerCompat.from(applicationContext).notify(channelId.hashCode(), notif)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS denied or revoked on API 33+
        }
    }

    private fun canPostNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val UNIQUE = "openascend_reminder_tick"

        fun schedule(context: Context) {
            val req = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE,
                ExistingPeriodicWorkPolicy.KEEP,
                req,
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE)
        }
    }
}
