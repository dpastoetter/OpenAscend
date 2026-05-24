package com.openascend.domain.insight

import com.openascend.domain.model.CoreStat
import com.openascend.domain.model.DailyMetric
import com.openascend.domain.model.StatBlock
import java.time.DayOfWeek
import java.time.LocalDate

object ChronicleInsightEngine {

    /**
     * Picks at most one insight for [weekStartEpochDay] using the prior ~30 days of data.
     */
    fun pickWeeklyInsight(
        weekStartEpochDay: Long,
        metricsByDay: Map<Long, DailyMetric>,
        statsByDay: Map<Long, StatBlock>,
        sealedEpochDays: Set<Long>,
    ): ChronicleInsight? {
        val candidates = mutableListOf<ChronicleInsight>()
        weekdayRecoverySlump(metricsByDay, statsByDay)?.let { candidates += it }
        disciplineStaminaCoupling(sealedEpochDays, statsByDay)?.let { candidates += it }
        stabilityStreak(metricsByDay)?.let { candidates += it }
        bestWeekHighlight(weekStartEpochDay, statsByDay)?.let { candidates += it }
        if (candidates.isEmpty()) return null
        val seed = weekStartEpochDay
        return candidates[(seed % candidates.size).toInt().coerceAtLeast(0)]
    }

    private fun weekdayRecoverySlump(
        metrics: Map<Long, DailyMetric>,
        stats: Map<Long, StatBlock>,
    ): ChronicleInsight? {
        if (stats.size < 14) return null
        val byDow = mutableMapOf<DayOfWeek, MutableList<Int>>()
        for ((epoch, block) in stats) {
            val dow = LocalDate.ofEpochDay(epoch).dayOfWeek
            byDow.getOrPut(dow) { mutableListOf() }.add(block.recovery)
        }
        val overall = stats.values.map { it.recovery }.average()
        val worst = byDow.minByOrNull { (_, vals) -> vals.average() } ?: return null
        val worstAvg = worst.value.average()
        if (overall - worstAvg < 8) return null
        val dayName = worst.key.name.lowercase().replaceFirstChar { it.titlecase() }
        return ChronicleInsight(
            id = "recovery_${worst.key.name.lowercase()}",
            headline = "$dayName tends to thin your Recovery",
            body = "Your chronicle shows Recovery dipping on ${dayName}s—try an earlier wind-down the night before.",
        )
    }

    private fun disciplineStaminaCoupling(
        sealedDays: Set<Long>,
        stats: Map<Long, StatBlock>,
    ): ChronicleInsight? {
        if (sealedDays.size < 5 || stats.size < 10) return null
        val sorted = sealedDays.sortedDescending().take(3)
        if (sorted.size < 2) return null
        val gaps = sorted.zipWithNext { a, b -> a - b }
        if (gaps.any { it > 1 }) return null
        val staminaOnSealed = sorted.mapNotNull { stats[it]?.stamina }
        if (staminaOnSealed.size < 2) return null
        val avgSealed = staminaOnSealed.average()
        val avgAll = stats.values.map { it.stamina }.average()
        if (avgSealed <= avgAll + 5) return null
        return ChronicleInsight(
            id = "discipline_stamina",
            headline = "Stamina rallies when you seal the chronicle",
            body = "On days you close the log, your Stamina line runs hotter—keep the evening rite streak alive.",
        )
    }

    private fun stabilityStreak(metrics: Map<Long, DailyMetric>): ChronicleInsight? {
        val recent = metrics.entries.sortedByDescending { it.key }.take(7)
        val notes = recent.count { !it.value.moneyNote.isNullOrBlank() || (it.value.bankControlScore ?: 0) >= 6 }
        if (notes < 3) return null
        return ChronicleInsight(
            id = "stability_streak",
            headline = "Your treasury notes are steady",
            body = "Three honest money beats this week—the Stability line feels less haunted.",
        )
    }

    private fun bestWeekHighlight(
        weekStart: Long,
        stats: Map<Long, StatBlock>,
    ): ChronicleInsight? {
        if (stats.size < 21) return null
        fun weekTotal(start: Long): Int {
            return (0L..6L).sumOf { off ->
                stats[start + off]?.let { it.recovery + it.stamina + it.discipline } ?: 0
            }
        }
        val thisWeek = weekTotal(weekStart)
        val lastWeek = weekTotal(weekStart - 7)
        if (thisWeek <= lastWeek || thisWeek < 80) return null
        return ChronicleInsight(
            id = "best_week",
            headline = "This week outshines the last",
            body = "Recovery, Stamina, and Discipline together crest higher than your prior seven days—the realm noticed.",
        )
    }
}
