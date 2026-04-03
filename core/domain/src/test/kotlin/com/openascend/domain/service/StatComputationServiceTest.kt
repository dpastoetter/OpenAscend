package com.openascend.domain.service

import com.openascend.domain.model.CoreStat
import com.openascend.domain.model.DailyMetric
import com.openascend.domain.model.Habit
import org.junit.Assert.assertEquals
import org.junit.Test

class StatComputationServiceTest {

    private val svc = StatComputationService()
    private val epoch = 20_000L

    @Test
    fun computeToday_nullMetric_usesNeutralDisciplineWhenNoHabits() {
        val stats = svc.computeToday(null, emptyList(), emptyMap())
        assertEquals(45, stats.recovery)
        assertEquals(45, stats.stamina)
        assertEquals(45, stats.stability)
        assertEquals(50, stats.discipline)
        assertEquals(40, stats.vitality)
    }

    @Test
    fun computeToday_sleepInSweetSpot_recoveryHigh() {
        val m = DailyMetric(epoch, 7.5f, null, null, null, null)
        val stats = svc.computeToday(m, emptyList(), emptyMap())
        assertEquals(85, stats.recovery)
    }

    @Test
    fun computeToday_stepsHigh_staminaHigh() {
        val m = DailyMetric(epoch, null, 10_000, null, null, null)
        val stats = svc.computeToday(m, emptyList(), emptyMap())
        assertEquals(90, stats.stamina)
    }

    @Test
    fun computeToday_bankControl_mapsToTenX() {
        val m = DailyMetric(epoch, null, null, 7, null, null)
        val stats = svc.computeToday(m, emptyList(), emptyMap())
        assertEquals(70, stats.stability)
    }

    @Test
    fun computeToday_halfHabitsDone_disciplineFiftyPercent() {
        val habits = listOf(
            Habit(1, "a", 7, 2, CoreStat.DISCIPLINE),
            Habit(2, "b", 7, 2, CoreStat.DISCIPLINE),
        )
        val stats = svc.computeToday(null, habits, mapOf(1L to true, 2L to false))
        assertEquals(50, stats.discipline)
    }

    @Test
    fun computeRollingSevenDay_emptyInputs_allNeutralFifty() {
        val stats = svc.computeRollingSevenDay(
            lastSevenDays = emptyList(),
            habits = emptyList(),
            isHabitCompleted = { _, _ -> false },
            todayEpochDay = epoch,
        )
        assertEquals(50, stats.recovery)
        assertEquals(50, stats.stamina)
        assertEquals(50, stats.discipline)
    }
}
