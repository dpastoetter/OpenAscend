package com.openascend.app.navigation

import android.net.Uri

object DeepLinkMapper {

    /**
     * Validates [Uri] before navigation: only the `openascend` scheme and a known host are accepted.
     * Ignores path/query/fragment so stray segments cannot influence routing.
     */
    fun validatedDeepLinkRoute(uri: Uri?): String? {
        if (uri == null) return null
        if (!uri.scheme.equals("openascend", ignoreCase = true)) return null
        val host = uri.host?.trim()?.lowercase() ?: return null
        if (host.isEmpty()) return null
        return routeFromHost(host)
    }

    /** Host from `openascend://{host}` → Nav route constant. */
    fun routeFromHost(host: String?): String? = when (host?.lowercase()) {
        "home" -> Routes.Home
        "check_in", "checkin" -> Routes.CheckIn
        "weekly" -> Routes.Weekly
        "boss" -> Routes.BossRitual
        "settings" -> Routes.Settings
        "character" -> Routes.Character
        "habits" -> Routes.Habits
        "companion", "companion_games" -> Routes.CompanionHub
        "companion_play", "companion_hide", "companion_hide_peek" -> Routes.CompanionPlay
        "companion_memory" -> Routes.CompanionMemory
        "companion_sequence" -> Routes.CompanionSequence
        "companion_glide" -> Routes.CompanionGlide
        "companion_stack" -> Routes.CompanionStack
        "companion_thread" -> Routes.CompanionThread
        "replay", "chronicle_replay" -> Routes.ChronicleReplay
        "duel", "chronicle_duel" -> Routes.ChronicleDuel
        "treasury", "treasury_ritual" -> Routes.TreasuryRitual
        "raid", "chronicle_raid" -> Routes.ChronicleRaid
        else -> null
    }
}
