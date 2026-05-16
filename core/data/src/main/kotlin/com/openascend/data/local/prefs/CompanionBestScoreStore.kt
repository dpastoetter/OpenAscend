package com.openascend.data.local.prefs

import android.content.Context

/** Per-game best scores for companion PB share prompts (local only). */
class CompanionBestScoreStore(context: Context) {
    private val p = context.applicationContext.getSharedPreferences("openascend_companion_pb", Context.MODE_PRIVATE)

    fun bestScore(gameKey: String): Int = p.getInt(key(gameKey), 0)

    /** @return true if this score is a new personal best */
    fun recordScore(gameKey: String, score: Int): Boolean {
        val prev = bestScore(gameKey)
        if (score <= prev) return false
        p.edit().putInt(key(gameKey), score).apply()
        return true
    }

    private fun key(gameKey: String) = "best_$gameKey"
}
