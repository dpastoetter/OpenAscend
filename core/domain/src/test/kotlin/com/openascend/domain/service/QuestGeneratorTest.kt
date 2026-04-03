package com.openascend.domain.service

import com.openascend.domain.model.CoreStat
import com.openascend.domain.model.StatBlock
import com.openascend.domain.narrative.NarrativeContext
import com.openascend.domain.narrative.NarrativePack
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class QuestGeneratorTest {

    private val gen = QuestGenerator()

    @Test
    fun recoveryChain_replacesRecoveryTitle() {
        val stats = StatBlock(10, 50, 50, 50, 50)
        val pack = NarrativePack.fallback()
        val ctx = NarrativeContext(LocalDate.of(2026, 1, 1), pack)
        val quests = gen.dailyQuests(
            stats = stats,
            goals = emptyList(),
            todayEpochDay = 20_100L,
            completions = emptySet(),
            narrative = ctx,
            recoveryChainActive = true,
        )
        val recovery = quests.first { it.linkedStat == CoreStat.RECOVERY }
        assertTrue(recovery.title.contains("Rite of three dawns"))
    }

    @Test
    fun flavorSuffix_appended() {
        val stats = StatBlock(10, 50, 50, 50, 50)
        val pack = NarrativePack(
            id = "x",
            actTitles = emptyList(),
            questActPrefix = "",
            bossTellTemplates = listOf("x"),
            questTitleFlavorSuffixes = listOf(" · x"),
        )
        val ctx = NarrativeContext(LocalDate.of(2026, 1, 1), pack)
        val quests = gen.dailyQuests(
            stats = stats,
            goals = emptyList(),
            todayEpochDay = 20_100L,
            completions = emptySet(),
            narrative = ctx,
        )
        assertTrue(quests.any { it.title.contains("· x") })
    }
}
