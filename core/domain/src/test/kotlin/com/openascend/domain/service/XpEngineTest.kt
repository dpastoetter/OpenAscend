package com.openascend.domain.service

import com.openascend.domain.model.StatBlock
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class XpEngineTest {

    @Test
    fun award_positive_appendsXp() = runBlocking {
        val repo = FakeXpRepository()
        val engine = XpEngine(repo, ArchetypeResolver())
        engine.award(15, "test")
        assertEquals(15, repo.totalXp())
    }

    @Test
    fun award_nonPositive_ignored() = runBlocking {
        val repo = FakeXpRepository()
        val engine = XpEngine(repo, ArchetypeResolver())
        engine.award(0, "none")
        engine.award(-5, "none")
        assertEquals(0, repo.totalXp())
    }

    @Test
    fun progressForStats_levelMatchesTotalXp() = runBlocking {
        val repo = FakeXpRepository()
        val engine = XpEngine(repo, ArchetypeResolver())
        repo.appendEvent(119, "grind")
        val stats = StatBlock(60, 60, 60, 60, 60)
        val p = engine.progressForStats(stats, streakDays = 0)
        assertEquals(1, p.level)
        assertEquals(119, p.xpInLevel)
        assertTrue(p.xpToNext >= 1)
    }

    @Test
    fun progressForStats_streakArmor_caps() = runBlocking {
        val repo = FakeXpRepository()
        val engine = XpEngine(repo, ArchetypeResolver())
        val stats = StatBlock(50, 50, 50, 50, 50)
        val p = engine.progressForStats(stats, streakDays = 100)
        assertEquals(45, p.streakArmor)
    }
}
