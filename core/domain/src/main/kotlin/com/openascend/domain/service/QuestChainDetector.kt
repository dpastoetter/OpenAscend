package com.openascend.domain.service

import com.openascend.domain.model.CoreStat

/**
 * True when the prior three epoch days each have at least one completed daily quest
 * tied to the given stat (matches [QuestGenerator] quest ids `dq_{day}_{STAT}_{idx}`).
 */
object QuestChainDetector {
    fun chainActiveForStat(
        stat: CoreStat,
        todayEpochDay: Long,
        completedIdsForDay: (Long) -> Set<String>,
    ): Boolean =
        (1L..3L).all { offset ->
            completedIdsForDay(todayEpochDay - offset).any { id ->
                id.startsWith("dq_") && id.contains("_${stat.name}_")
            }
        }

    fun recoveryChainActive(
        todayEpochDay: Long,
        completedIdsForDay: (Long) -> Set<String>,
    ): Boolean = chainActiveForStat(CoreStat.RECOVERY, todayEpochDay, completedIdsForDay)

    fun activeChains(
        todayEpochDay: Long,
        completedIdsForDay: (Long) -> Set<String>,
    ): Set<CoreStat> = CoreStat.entries.filterTo(mutableSetOf()) { stat ->
        chainActiveForStat(stat, todayEpochDay, completedIdsForDay)
    }
}
