package com.openascend.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Habit(
    val id: Long,
    val name: String,
    val frequencyPerWeek: Int,
    val difficulty: Int,
    val linkedStat: CoreStat,
    val isRestDay: Boolean = false,
)

data class HabitDraft(
    val name: String,
    val frequencyPerWeek: Int,
    val difficulty: Int,
    val linkedStat: CoreStat,
    val isRestDay: Boolean = false,
)
