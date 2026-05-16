package com.openascend.domain.service

import org.junit.Assert.assertEquals
import org.junit.Test

class HabitRewardsTest {

    @Test
    fun xp_scalesWithDifficulty() {
        assertEquals(2, HabitRewards.xpForDifficulty(1))
        assertEquals(5, HabitRewards.xpForDifficulty(5))
    }
}
