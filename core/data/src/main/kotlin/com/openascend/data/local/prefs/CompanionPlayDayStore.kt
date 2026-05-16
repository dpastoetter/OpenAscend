package com.openascend.data.local.prefs

import android.content.Context

/** Tracks calendar days with any companion play (cosmetic streak on Home). */
class CompanionPlayDayStore(context: Context) {
    private val p = context.applicationContext.getSharedPreferences("openascend_companion_play", Context.MODE_PRIVATE)

    fun recordPlayDay(epochDay: Long) {
        val csv = p.getString(KEY_DAYS, "").orEmpty()
        val set = csv.split(',').filter { it.isNotBlank() }.map { it.toLong() }.toMutableSet()
        set.add(epochDay)
        val trimmed = set.sorted().takeLast(14).joinToString(",")
        p.edit().putString(KEY_DAYS, trimmed).apply()
    }

    fun consecutivePlayDaysEnding(todayEpochDay: Long): Int {
        val set = p.getString(KEY_DAYS, "").orEmpty()
            .split(',')
            .filter { it.isNotBlank() }
            .map { it.toLong() }
            .toSet()
        var count = 0
        var d = todayEpochDay
        while (d in set) {
            count++
            d--
        }
        return count
    }

    companion object {
        private const val KEY_DAYS = "play_epoch_days"
    }
}
