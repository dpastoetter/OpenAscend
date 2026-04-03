package com.openascend.domain.model

data class CharacterProgress(
    val level: Int,
    val xpInLevel: Int,
    val xpToNext: Int,
    val totalXp: Int,
    val archetype: PlayerArchetype,
    /** Narrative shield from streaks — caps at a fun ceiling */
    val streakArmor: Int,
)
