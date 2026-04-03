package com.openascend.domain.service

import org.junit.Assert.assertEquals
import org.junit.Test

class LevelCurveTest {

    @Test
    fun xpRequired_level1_matchesFormula() {
        assertEquals(120, LevelCurve.xpRequiredForLevel(1))
    }

    @Test
    fun xpRequired_level2_matchesFormula() {
        assertEquals(120 + 1 * (40 + 8), LevelCurve.xpRequiredForLevel(2))
    }

    @Test
    fun levelFromTotalXp_zero_isLevelOne() {
        val (level, inLevel) = LevelCurve.levelFromTotalXp(0)
        assertEquals(1, level)
        assertEquals(0, inLevel)
    }

    @Test
    fun levelFromTotalXp_oneBelowFirstThreshold_staysLevelOne() {
        val (level, inLevel) = LevelCurve.levelFromTotalXp(119)
        assertEquals(1, level)
        assertEquals(119, inLevel)
    }

    @Test
    fun levelFromTotalXp_exactlyFirstThreshold_startsLevelTwo() {
        val (level, inLevel) = LevelCurve.levelFromTotalXp(120)
        assertEquals(2, level)
        assertEquals(0, inLevel)
    }

    @Test
    fun xpToNextLevel_neverNegative() {
        assertEquals(0, LevelCurve.xpToNextLevel(1, 120))
        assertEquals(1, LevelCurve.xpToNextLevel(1, 119))
    }
}
