package com.openascend.domain.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestChainDetectorTest {

    @Test
    fun activeWhenThreePriorDaysHaveRecoveryQuests() {
        val today = 100L
        val map = mapOf(
            99L to setOf("dq_99_RECOVERY_0"),
            98L to setOf("dq_98_RECOVERY_0"),
            97L to setOf("dq_97_RECOVERY_1"),
        )
        assertTrue(
            QuestChainDetector.recoveryChainActive(today) { d -> map[d].orEmpty() },
        )
    }

    @Test
    fun inactiveWhenOneDayMissing() {
        val today = 100L
        val map = mapOf(
            99L to setOf("dq_99_RECOVERY_0"),
            98L to setOf("dq_98_STAMINA_0"),
            97L to setOf("dq_97_RECOVERY_0"),
        )
        assertFalse(
            QuestChainDetector.recoveryChainActive(today) { d -> map[d].orEmpty() },
        )
    }
}
