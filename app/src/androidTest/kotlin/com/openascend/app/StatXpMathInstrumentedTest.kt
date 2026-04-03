package com.openascend.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.openascend.domain.model.CoreStat
import dagger.hilt.android.testing.HiltAndroidTest
import com.openascend.domain.model.DailyMetric
import com.openascend.domain.model.Habit
import com.openascend.domain.service.LevelCurve
import com.openascend.domain.service.StatComputationService
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented smoke tests that exercise the same stat/XP domain math loaded on device.
 * Full coverage lives in `:core:domain` JVM unit tests (`./gradlew :core:domain:test`).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class StatXpMathInstrumentedTest {

    @Test
    fun levelCurve_firstTier() {
        assertEquals(120, LevelCurve.xpRequiredForLevel(1))
        val (level, inLevel) = LevelCurve.levelFromTotalXp(0)
        assertEquals(1, level)
        assertEquals(0, inLevel)
    }

    @Test
    fun statComputation_sleepSweetSpot() {
        val svc = StatComputationService()
        val m = DailyMetric(19_000L, 7.2f, null, null, null, null)
        assertEquals(85, svc.computeToday(m, emptyList(), emptyMap()).recovery)
    }

    @Test
    fun statComputation_disciplineHalfHabits() {
        val svc = StatComputationService()
        val habits = listOf(
            Habit(1L, "a", 7, 2, CoreStat.DISCIPLINE),
            Habit(2L, "b", 7, 2, CoreStat.DISCIPLINE),
        )
        assertEquals(
            50,
            svc.computeToday(null, habits, mapOf(1L to true, 2L to false)).discipline,
        )
    }
}
