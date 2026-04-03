package com.openascend.domain.narrative

/**
 * Three cosmetic suffix choices per level band (plan: 3, 6, 10).
 */
object ArchetypeSuffixCatalog {
    fun choicesForBand(bandLevel: Int): List<String> = when (bandLevel) {
        3 -> listOf("of the First Ember", "of the Open Road", "of the Quiet Ledger")
        6 -> listOf("Bearer of Small Oaths", "Keeper of the Steady Flame", "Warden of Gentle Momentum")
        10 -> listOf("Ascendant-in-Training", "Scribe of the Long Game", "Shield of the Soft Hour")
        else -> emptyList()
    }

    fun bandForLevel(level: Int): Int? = when {
        level >= 10 -> 10
        level >= 6 -> 6
        level >= 3 -> 3
        else -> null
    }
}
