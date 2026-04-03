package com.openascend.domain.model

enum class QuestTier(val label: String, val stars: Int) {
    COMMON("Common", 1),
    UNCOMMON("Uncommon", 2),
    RARE("Rare", 3),
    EPIC("Epic", 4),
    ;

    companion object {
        fun fromDifficulty(difficulty: Int): QuestTier = when (difficulty.coerceIn(1, 5)) {
            1 -> COMMON
            2 -> UNCOMMON
            3 -> RARE
            4, 5 -> EPIC
            else -> COMMON
        }
    }
}
