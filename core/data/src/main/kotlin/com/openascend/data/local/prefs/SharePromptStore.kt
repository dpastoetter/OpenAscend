package com.openascend.data.local.prefs

import android.content.Context

/** Caps automatic share nudges to once per calendar day (manual share buttons are always available). */
class SharePromptStore(context: Context) {
    private val p = context.applicationContext.getSharedPreferences("openascend_share", Context.MODE_PRIVATE)

    fun canShowAutoNudge(epochDay: Long): Boolean {
        val last = p.getLong(KEY_AUTO_NUDGE_DAY, Long.MIN_VALUE)
        return last != epochDay
    }

    fun recordAutoNudge(epochDay: Long) {
        p.edit().putLong(KEY_AUTO_NUDGE_DAY, epochDay).apply()
    }

    companion object {
        private const val KEY_AUTO_NUDGE_DAY = "auto_nudge_epoch_day"
    }
}
