package com.openascend.domain.service

import com.openascend.domain.model.CoreStat
import com.openascend.domain.model.Habit
import org.junit.Assert.assertEquals
import org.junit.Test

class BossPrepMeterTest {

    private val prepHabit = Habit(1, "prep", 7, 3, CoreStat.DISCIPLINE, bossPrep = true)
    private val normal = Habit(2, "normal", 7, 2, CoreStat.DISCIPLINE)

    @Test
    fun countPrepSeals_onlyBossPrepHabitsInWeek() {
        val weekStart = 100L
        val today = 102L
        val count = BossPrepMeter.countPrepSealsThisWeek(
            habits = listOf(prepHabit, normal),
            weekStartEpochDay = weekStart,
            todayEpochDay = today,
            isCompleted = { id, day -> id == 1L && day == 101L },
        )
        assertEquals(1, count)
    }

    @Test
    fun bonusXp_cappedAtFifteen() {
        assertEquals(15, BossPrepMeter.bonusXpForPrepSeals(10))
        assertEquals(10, BossPrepMeter.bonusXpForPrepSeals(2))
    }
}
