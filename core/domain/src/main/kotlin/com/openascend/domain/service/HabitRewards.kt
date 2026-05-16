package com.openascend.domain.service

import com.openascend.domain.model.QuestTier

/** Small XP grants when a habit is sealed for the day (once per habit per day). */
object HabitRewards {
    fun xpForDifficulty(difficulty: Int): Int = when (QuestTier.fromDifficulty(difficulty)) {
        QuestTier.COMMON -> 2
        QuestTier.UNCOMMON -> 3
        QuestTier.RARE -> 4
        QuestTier.EPIC -> 5
    }
}
