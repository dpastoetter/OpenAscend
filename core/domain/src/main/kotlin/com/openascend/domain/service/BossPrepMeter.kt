package com.openascend.domain.service

import com.openascend.domain.model.Habit

/** Tracks boss-prep habit seals in the current boss week for ritual bonuses. */
object BossPrepMeter {
    const val XP_PER_PREP_SEAL = 5
    const val MAX_BONUS_XP = 15

    fun countPrepSealsThisWeek(
        habits: List<Habit>,
        weekStartEpochDay: Long,
        todayEpochDay: Long,
        isCompleted: (habitId: Long, epochDay: Long) -> Boolean,
    ): Int {
        val prepIds = habits.filter { it.bossPrep }.map { it.id }
        if (prepIds.isEmpty()) return 0
        var total = 0
        var d = weekStartEpochDay
        val weekEnd = weekStartEpochDay + 6
        while (d <= todayEpochDay && d <= weekEnd) {
            for (id in prepIds) {
                if (isCompleted(id, d)) total++
            }
            d++
        }
        return total
    }

    fun bonusXpForPrepSeals(prepSealsThisWeek: Int): Int =
        (prepSealsThisWeek * XP_PER_PREP_SEAL).coerceAtMost(MAX_BONUS_XP)
}
