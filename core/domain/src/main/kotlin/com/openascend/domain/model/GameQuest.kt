package com.openascend.domain.model

data class GameQuest(
    val id: String,
    val title: String,
    val description: String,
    val linkedStat: CoreStat,
    val xpReward: Int,
    val completed: Boolean,
)

/** Shown stat bump on Home when a daily quest is sealed (theater on top of signal-based stats). */
object QuestDisplayBonus {
    const val PER_SEALED_QUEST = 5
}
