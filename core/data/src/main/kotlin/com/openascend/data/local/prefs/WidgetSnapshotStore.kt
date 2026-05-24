package com.openascend.data.local.prefs

import android.content.Context

/**
 * Simple snapshot for home-screen widgets (same-process read; no Hilt in Glance receiver).
 */
class WidgetSnapshotStore(
    context: Context,
) {
    private val p = context.applicationContext.getSharedPreferences("openascend_widget", Context.MODE_PRIVATE)

    fun write(
        level: Int,
        questTitle: String,
        bossName: String,
        flavorLine: String,
        dailyBoonAvailable: Boolean = false,
        actionUri: String = "openascend://home",
        streakDays: Int = 0,
        bossSealedThisWeek: Boolean = false,
        seasonLine: String = "",
    ) {
        p.edit()
            .putInt("level", level)
            .putString("quest", questTitle.take(120))
            .putString("boss", bossName.take(120))
            .putString("flavor", flavorLine.take(200))
            .putBoolean("daily_boon", dailyBoonAvailable)
            .putString("action_uri", actionUri)
            .putInt("streak_days", streakDays)
            .putBoolean("boss_sealed", bossSealedThisWeek)
            .putString("season_line", seasonLine.take(120))
            .apply()
    }

    fun readLevel(): Int = p.getInt("level", 1)
    fun readQuestTitle(): String = p.getString("quest", "—") ?: "—"
    fun readBossName(): String = p.getString("boss", "—") ?: "—"
    fun readFlavorLine(): String = p.getString("flavor", "—") ?: "—"
    fun readDailyBoonAvailable(): Boolean = p.getBoolean("daily_boon", false)
    fun readActionUri(): String = p.getString("action_uri", "openascend://home") ?: "openascend://home"
    fun readStreakDays(): Int = p.getInt("streak_days", 0)
    fun readBossSealedThisWeek(): Boolean = p.getBoolean("boss_sealed", false)
    fun readSeasonLine(): String = p.getString("season_line", "") ?: ""
}
