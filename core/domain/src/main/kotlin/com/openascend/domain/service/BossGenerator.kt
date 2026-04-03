package com.openascend.domain.service

import com.openascend.domain.model.CoreStat
import com.openascend.domain.model.StatBlock
import com.openascend.domain.model.WeeklyBoss
import com.openascend.domain.narrative.NarrativeContext
import com.openascend.domain.narrative.NarrativePack

class BossGenerator {
    fun weeklyBoss(
        weekStartEpochDay: Long,
        stats: StatBlock,
        narrative: NarrativeContext? = null,
        bossDeferredForThisWeek: Boolean = false,
    ): WeeklyBoss {
        val pack = narrative?.pack ?: NarrativePack.fallback()
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
        val deferredFlavor = if (bossDeferredForThisWeek) {
            "You asked for a gentler week—the tale thins your streak-armor, but the realm grants reprieve."
        } else {
            flavor
        }
        val tell = if (bossDeferredForThisWeek) {
            "${name} lingers at the edge of the map; your oath was to rest this week."
        } else {
            formatTell(
                templates = pack.bossTellTemplates,
                seed = weekStartEpochDay + target.ordinal * 31L,
                bossName = name,
                stat = target,
            )
        }
        return WeeklyBoss(
            weekEpochDayStart = weekStartEpochDay,
            name = name,
            flavor = deferredFlavor,
            tell = tell,
            targetStat = target,
            suggestedActions = tips,
        )
    }

    private fun formatTell(
        templates: List<String>,
        seed: Long,
        bossName: String,
        stat: CoreStat,
    ): String {
        val list = templates.ifEmpty { listOf("{boss} stirs when {stat} runs thin.") }
        val template = list[(kotlin.math.abs(seed) % list.size).toInt()]
        return template
            .replace("{boss}", bossName)
            .replace("{stat}", stat.narrativeLabel())
    }
}

private fun CoreStat.narrativeLabel(): String = when (this) {
    CoreStat.RECOVERY -> "recovery"
    CoreStat.STAMINA -> "stamina"
    CoreStat.STABILITY -> "stability"
    CoreStat.DISCIPLINE -> "discipline"
    CoreStat.VITALITY -> "vitality"
}
