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
    /** When true, copy nudges tying this habit to the weekly boss prep arc. */
    val bossPrep: Boolean = false,
)

data class HabitDraft(
    val name: String,
    val frequencyPerWeek: Int,
    val difficulty: Int,
    val linkedStat: CoreStat,
    val isRestDay: Boolean = false,
    val bossPrep: Boolean = false,
)
