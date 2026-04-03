package com.openascend.domain.service

import com.openascend.domain.model.CharacterProgress
import com.openascend.domain.model.StatBlock
import com.openascend.domain.repository.XpRepository

class XpEngine(
    private val xpRepository: XpRepository,
    private val archetypeResolver: ArchetypeResolver,
) {
    suspend fun award(amount: Int, reason: String) {
        if (amount <= 0) return
        xpRepository.appendEvent(amount, reason)
    }

    suspend fun progressForStats(stats: StatBlock, streakDays: Int): CharacterProgress {
        val total = xpRepository.totalXp()
        val (level, xpInLevel) = LevelCurve.levelFromTotalXp(total)
        val xpToNext = LevelCurve.xpToNextLevel(level, xpInLevel)
        val archetype = archetypeResolver.resolve(stats)
        val armor = streakArmor(streakDays)
        return CharacterProgress(
            level = level,
            xpInLevel = xpInLevel,
            xpToNext = xpToNext,
            totalXp = total,
            archetype = archetype,
            streakArmor = armor,
        )
    }

    private fun streakArmor(streakDays: Int): Int =
        (streakDays * 3).coerceIn(0, 45)
}
