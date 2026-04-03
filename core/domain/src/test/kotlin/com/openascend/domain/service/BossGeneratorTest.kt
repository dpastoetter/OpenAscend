package com.openascend.domain.service

import com.openascend.domain.model.CoreStat
import com.openascend.domain.model.StatBlock
import com.openascend.domain.narrative.NarrativeContext
import com.openascend.domain.narrative.NarrativePack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BossGeneratorTest {

    private val gen = BossGenerator()

    @Test
    fun tell_isDeterministicForWeekAndWeakestStat() {
        val stats = StatBlock(10, 50, 50, 50, 50)
        val pack = NarrativePack(
            id = "t",
            actTitles = emptyList(),
            questActPrefix = "",
            bossTellTemplates = listOf("{boss} watches {stat}."),
            questTitleFlavorSuffixes = listOf(""),
        )
        val ctx = NarrativeContext(LocalDate.of(2026, 6, 15), pack)
        val a = gen.weeklyBoss(19_000L, stats, ctx)
        val b = gen.weeklyBoss(19_000L, stats, ctx)
        assertEquals(a.tell, b.tell)
        assertTrue(a.tell.contains("Sleepless"))
        assertTrue(a.tell.contains("recovery"))
    }

    @Test
    fun deferredWeek_usesSofterTell() {
        val stats = StatBlock(10, 50, 50, 50, 50)
        val boss = gen.weeklyBoss(20_000L, stats, bossDeferredForThisWeek = true)
        assertTrue(boss.tell.contains("edge of the map"))
    }
}
