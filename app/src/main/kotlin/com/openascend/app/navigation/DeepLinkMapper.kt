package com.openascend.app.navigation

object DeepLinkMapper {
    /** Host from `openascend://{host}` → Nav route constant. */
    fun routeFromHost(host: String?): String? = when (host?.lowercase()) {
        "home" -> Routes.Home
        "check_in", "checkin" -> Routes.CheckIn
        "weekly" -> Routes.Weekly
        "boss" -> Routes.BossRitual
        "settings" -> Routes.Settings
        "character" -> Routes.Character
        "habits" -> Routes.Habits
        else -> null
    }
}
