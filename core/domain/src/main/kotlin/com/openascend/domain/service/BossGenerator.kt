package com.openascend.domain.service

import com.openascend.domain.model.CoreStat
import com.openascend.domain.model.StatBlock
import com.openascend.domain.model.WeeklyBoss

class BossGenerator {
    fun weeklyBoss(weekStartEpochDay: Long, stats: StatBlock): WeeklyBoss {
        val target = stats.weakestStat()
        val (name, flavor, tips) = when (target) {
            CoreStat.RECOVERY -> Triple(
                "The Sleepless Warden",
                "A phantom of skipped rest — it feeds on late screens and early alarms.",
                listOf(
                    "Set a non-negotiable lights-out alarm.",
                    "Swap one scroll session for a wind-down ritual.",
                ),
            )
            CoreStat.STAMINA -> Triple(
                "The Stillness Colossus",
                "Gravity itself seems heavier when movement fades.",
                listOf(
                    "Book two 10-minute movement breaks.",
                    "Take stairs once where you normally would not.",
                ),
            )
            CoreStat.STABILITY -> Triple(
                "The Ledger Lich",
                "It whispers doubt whenever numbers feel fuzzy.",
                listOf(
                    "One honest money note in OpenAscend.",
                    "Name one win and one leak from the week.",
                ),
            )
            CoreStat.DISCIPLINE -> Triple(
                "The Procrastination Hydra",
                "Each avoided task sprouts another excuse-head.",
                listOf(
                    "Clear the smallest habit first for momentum.",
                    "Stack a habit onto something you already do daily.",
                ),
            )
            CoreStat.VITALITY -> Triple(
                "The Rust Wyrm",
                "Long-view upkeep ignored becomes corrosion.",
                listOf(
                    "One vitality check-in; keep it kind, not clinical.",
                    "Add a micro-habit that compounds (sleep, steps, or calm).",
                ),
            )
        }
        return WeeklyBoss(
            weekEpochDayStart = weekStartEpochDay,
            name = name,
            flavor = flavor,
            targetStat = target,
            suggestedActions = tips,
        )
    }
}
