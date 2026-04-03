package com.openascend.domain.model

data class GameQuest(
    val id: String,
    val title: String,
    val description: String,
    val linkedStat: CoreStat,
    val xpReward: Int,
    val completed: Boolean,
)
