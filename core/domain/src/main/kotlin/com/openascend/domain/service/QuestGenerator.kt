package com.openascend.domain.service

import com.openascend.domain.model.CoreStat
import com.openascend.domain.model.GameQuest
import com.openascend.domain.model.StatBlock

class QuestGenerator {
    fun dailyQuests(
        stats: StatBlock,
        goals: List<String>,
        todayEpochDay: Long,
        completions: Set<String>,
    ): List<GameQuest> {
        val weak = stats.asMap().entries.sortedBy { it.value }.take(2).map { it.key }
        val fromWeak = weak.mapIndexed { idx, stat ->
            templateFor(stat, idx, todayEpochDay, completions)
        }
        val goalQuest = goals.take(1).mapIndexed { idx, g ->
            GameQuest(
                id = "goal_${todayEpochDay}_$idx",
                title = "Quest: ${g.take(32)}",
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
        day: Long,
        completions: Set<String>,
    ): GameQuest {
        val id = "dq_${day}_${stat.name}_$idx"
        val (title, desc, xp) = when (stat) {
            CoreStat.RECOVERY -> Triple(
                "Sanctuary Hour",
                "Wind down 30 minutes earlier than usual tonight.",
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
        return GameQuest(
            id = id,
            title = title,
            description = desc,
            linkedStat = stat,
            xpReward = xp,
            completed = completions.contains(id),
        )
    }
}
