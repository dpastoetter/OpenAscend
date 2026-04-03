package com.openascend.data.local.prefs

import android.content.Context

/**
 * Simple snapshot for home-screen widgets (same-process read; no Hilt in Glance receiver).
 */
class WidgetSnapshotStore(
    context: Context,
) {
    private val p = context.applicationContext.getSharedPreferences("openascend_widget", Context.MODE_PRIVATE)

    fun write(level: Int, questTitle: String, bossName: String) {
        p.edit()
            .putInt("level", level)
            .putString("quest", questTitle.take(120))
            .putString("boss", bossName.take(120))
            .apply()
    }

    fun readLevel(): Int = p.getInt("level", 1)
    fun readQuestTitle(): String = p.getString("quest", "—") ?: "—"
    fun readBossName(): String = p.getString("boss", "—") ?: "—"
}
