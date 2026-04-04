package com.openascend.domain.service

import com.openascend.domain.model.CoreStat
import com.openascend.domain.model.GameQuest
import com.openascend.domain.model.StatBlock
import com.openascend.domain.narrative.NarrativeContext
import com.openascend.domain.narrative.NarrativePack
import java.time.DayOfWeek
import java.time.LocalDate

class QuestGenerator {
    fun dailyQuests(
        stats: StatBlock,
        goals: List<String>,
        todayEpochDay: Long,
        completions: Set<String>,
        narrative: NarrativeContext? = null,
        recoveryChainActive: Boolean = false,
    ): List<GameQuest> {
        val localDate = LocalDate.ofEpochDay(todayEpochDay)
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
        val merged = (fromWeak + goalQuest).toMutableList()
        if (localDate.dayOfWeek == DayOfWeek.TUESDAY || localDate.dayOfWeek == DayOfWeek.FRIDAY) {
            val wild = wildCardQuest(todayEpochDay, completions, pack)
            if (merged.size >= 4) merged[merged.lastIndex] = wild else merged.add(wild)
        }
        return merged.take(4)
    }

    private fun wildCardQuest(
        todayEpochDay: Long,
        completions: Set<String>,
        pack: NarrativePack,
    ): GameQuest {
        val variants = listOf(
            Triple(
                CoreStat.VITALITY,
                "Moonlit errand",
                "Do one small kindness for your body—water, light, or five slow breaths.",
            ),
            Triple(
                CoreStat.STAMINA,
                "Wanderer's gambit",
                "Take a ten-minute loop outside or pace while a kettle boils.",
            ),
            Triple(
                CoreStat.STABILITY,
                "Quiet ledger",
                "Name one spend you're glad about and one you'd soften next time (mental note is fine).",
            ),
            Triple(
                CoreStat.DISCIPLINE,
                "Wildcard oath",
                "Finish a two-minute task you've been side-eyeing.",
            ),
            Triple(
                CoreStat.RECOVERY,
                "Soft checkpoint",
                "Dim a screen or swap one scroll for silence before bed.",
            ),
        )
        val pick = variants[todayEpochDay.mod(variants.size.toLong()).toInt()]
        val id = "wild_${todayEpochDay}_${pick.first.name}"
        val title = flavorTitle(pick.second, pack, todayEpochDay, 90)
        val prefix = pack.questActPrefix.trim()
        val titled = if (prefix.isNotEmpty()) "$prefix $title" else title
        return GameQuest(
            id = id,
            title = titled,
            description = pick.third,
            linkedStat = pick.first,
            xpReward = 22,
            completed = completions.contains(id),
        )
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
