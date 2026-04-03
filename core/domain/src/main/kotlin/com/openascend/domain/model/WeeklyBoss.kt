package com.openascend.domain.model

data class WeeklyBoss(
    val weekEpochDayStart: Long,
    val name: String,
    val flavor: String,
    val targetStat: CoreStat,
    val suggestedActions: List<String>,
)
