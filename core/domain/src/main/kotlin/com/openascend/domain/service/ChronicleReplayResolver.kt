package com.openascend.domain.service

import com.openascend.domain.model.DailyMetric
import com.openascend.domain.model.Habit
import com.openascend.domain.model.StatBlock

data class ChronicleReplayDay(
    val epochDay: Long,
    val stats: StatBlock,
    val moodHeadline: String?,
    val sealed: Boolean,
)

object ChronicleReplayResolver {
    fun buildLastDays(
        todayEpochDay: Long,
        days: Int,
        metricsByDay: Map<Long, DailyMetric?>, // epoch day -> metric (nullable)
        habits: List<Habit>,
        habitCompleted: (habitId: Long, epochDay: Long) -> Boolean,
        questCompleted: (epochDay: Long) -> Boolean,
        moodHeadlineForDay: (epochDay: Long) -> String?,
        computeStats: (metric: DailyMetric?, habits: List<Habit>, completions: Map<Long, Boolean>) -> StatBlock,
    ): List<ChronicleReplayDay> {
        return (0 until days).map { offset ->
            val day = todayEpochDay - (days - 1L - offset)
            val metric = metricsByDay[day]
            val completions = habits.associate { it.id to habitCompleted(it.id, day) }
            val stats = computeStats(metric, habits, completions)
            ChronicleReplayDay(
                epochDay = day,
                stats = stats,
                moodHeadline = moodHeadlineForDay(day),
                sealed = questCompleted(day) || completions.values.any { it },
            )
        }
    }
}
