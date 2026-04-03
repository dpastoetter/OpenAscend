package com.openascend.domain.service

import com.openascend.domain.model.CoreStat
import com.openascend.domain.model.GameQuest
import com.openascend.domain.model.StatBlock
import com.openascend.domain.narrative.NarrativeContext
import com.openascend.domain.narrative.NarrativePack

class QuestGenerator {
    fun dailyQuests(
        stats: StatBlock,
        goals: List<String>,
        todayEpochDay: Long,
        completions: Set<String>,
        narrative: NarrativeContext? = null,
        recoveryChainActive: Boolean = false,
    ): List<GameQuest> {
        val pack = narrative?.pack ?: NarrativePack.fallback()
        val weak = stats.asMap().entries.sortedBy { it.value }.take(2).map { it.key }
        val fromWeak = weak.mapIndexed { idx, stat ->
            templateFor(
                stat = stat,
                idx = idx,
                todayEpochDay = todayEpochDay,
                completions = completions,
                pack = pack,
                recoveryChainActive = recoveryChainActive && stat == CoreStat.RECOVERY,
            )
        }
        val goalQuest = goals.take(1).mapIndexed { idx, g ->
            val rawTitle = "Quest: ${g.take(32)}"
            GameQuest(
                id = "goal_${todayEpochDay}_$idx",
                title = flavorTitle(rawTitle, pack, todayEpochDay, idx + 10),
                description = "One honest rep toward: $g",
                linkedStat = CoreStat.DISCIPLINE,
                xpReward = 25,
                completed = completions.contains("goal_${todayEpochDay}_$idx"),
            )
        }
        return (fromWeak + goalQuest).take(4)
    }

    private fun templateFor(
        stat: CoreStat,
        idx: Int,
        todayEpochDay: Long,
        completions: Set<String>,
        pack: NarrativePack,
        recoveryChainActive: Boolean,
    ): GameQuest {
        val id = "dq_${todayEpochDay}_${stat.name}_$idx"
        val (baseTitle, baseDesc, xp) = when (stat) {
            CoreStat.RECOVERY -> Triple(
                if (recoveryChainActive) "Rite of three dawns" else "Sanctuary Hour",
                if (recoveryChainActive) {
                    "Third dawn in your recovery chain—the same kind rite: wind down 30 minutes earlier tonight."
                } else {
                    "Wind down 30 minutes earlier than usual tonight."
                },
                20,
            )
            CoreStat.STAMINA -> Triple(
                "Trailblazer Steps",
                "Add one short walk or 1k extra steps today.",
                20,
            )
            CoreStat.STABILITY -> Triple(
                "Ledger Check",
                "Log how you felt about spending today (1 line).",
                20,
            )
            CoreStat.DISCIPLINE -> Triple(
                "Oath of the Day",
                "Complete one habit you have been dodging.",
                25,
            )
            CoreStat.VITALITY -> Triple(
                "Vitality Rite",
                "Hydrate + one stretch block; optional vitality check-in.",
                20,
            )
        }
        val title = flavorTitle(baseTitle, pack, todayEpochDay, idx)
        val prefix = pack.questActPrefix.trim()
        val titled = if (prefix.isNotEmpty()) "$prefix $title" else title
        return GameQuest(
            id = id,
            title = titled,
            description = baseDesc,
            linkedStat = stat,
            xpReward = xp,
            completed = completions.contains(id),
        )
    }

    private fun flavorTitle(
        base: String,
        pack: NarrativePack,
        day: Long,
        salt: Int,
    ): String {
        val suffixes = pack.questTitleFlavorSuffixes.ifEmpty { listOf("") }
        val suffix = suffixes[((day + salt) % suffixes.size).toInt()].trim()
        return if (suffix.isEmpty()) base else "$base$suffix"
    }
}
