package com.openascend.domain.model

data class WeeklyBoss(
    val weekEpochDayStart: Long,
    val name: String,
    val flavor: String,
    /** One-line narrative whisper shown above the boss card. */
    val tell: String,
    val targetStat: CoreStat,
    val suggestedActions: List<String>,
)
