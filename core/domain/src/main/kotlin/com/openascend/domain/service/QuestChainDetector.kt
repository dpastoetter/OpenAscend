package com.openascend.domain.service

/**
 * True when the prior three epoch days each have at least one completed daily quest
 * tied to [CoreStat.RECOVERY] (matches [QuestGenerator] quest ids).
 */
object QuestChainDetector {
    fun recoveryChainActive(
        todayEpochDay: Long,
        completedIdsForDay: (Long) -> Set<String>,
    ): Boolean =
        (1L..3L).all { offset ->
            completedIdsForDay(todayEpochDay - offset).any { id ->
                id.startsWith("dq_") && id.contains("_RECOVERY_")
            }
        }
}
