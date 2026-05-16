package com.openascend.domain.service

object StreakMilestone {
    val thresholds = listOf(7, 14, 30)

    fun milestoneDays(streakDays: Int): Int? =
        if (streakDays in thresholds) streakDays else null
}
