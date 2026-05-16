package com.openascend.data.local.prefs

import android.content.Context
import com.openascend.domain.companion.CompanionMood

/**
 * Rolling window of companion moods (epochDay:moodOrdinal), max 7 entries.
 */
class CompanionMemoryStore(context: Context) {
    private val p = context.applicationContext.getSharedPreferences(
        "openascend_companion_memory",
        Context.MODE_PRIVATE,
    )

    fun append(epochDay: Long, mood: CompanionMood) {
        val entries = readEntries().filter { it.first != epochDay }
        val updated = (entries + (epochDay to mood.ordinal))
            .sortedByDescending { it.first }
            .take(7)
        p.edit()
            .putString(KEY, updated.joinToString("|") { "${it.first}:${it.second}" })
            .apply()
    }

    fun recentMoods(): List<CompanionMood> =
        readEntries()
            .sortedByDescending { it.first }
            .mapNotNull { (_, ord) -> CompanionMood.entries.getOrNull(ord) }

    private fun readEntries(): List<Pair<Long, Int>> {
        val raw = p.getString(KEY, null) ?: return emptyList()
        return raw.split("|").mapNotNull { part ->
            val bits = part.split(":")
            if (bits.size != 2) return@mapNotNull null
            val day = bits[0].toLongOrNull() ?: return@mapNotNull null
            val ord = bits[1].toIntOrNull() ?: return@mapNotNull null
            day to ord
        }
    }

    companion object {
        private const val KEY = "mood_log"
    }
}
