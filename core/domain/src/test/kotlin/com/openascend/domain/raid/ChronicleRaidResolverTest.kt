package com.openascend.domain.raid

import com.openascend.domain.model.StatBlock
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChronicleRaidResolverTest {

    @Test
    fun strongParty_succeeds() {
        val block = StatBlock(60, 60, 60, 60, 60)
        val result = ChronicleRaidResolver.resolve(
            bossName = "Test Boss",
            members = listOf("A" to block, "B" to block),
        )
        assertTrue(result.success)
        assertTrue(result.teamPower >= result.threshold)
    }

    @Test
    fun weakParty_fails() {
        val block = StatBlock(10, 10, 10, 10, 10)
        val result = ChronicleRaidResolver.resolve(
            bossName = "Test Boss",
            members = listOf("A" to block),
        )
        assertFalse(result.success)
    }
}
