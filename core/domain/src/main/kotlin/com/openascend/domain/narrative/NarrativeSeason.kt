package com.openascend.domain.narrative

data class NarrativeSeason(
    val id: String,
    val title: String,
    val weekThemes: List<String>,
    val finaleBossName: String,
)

data class SeasonProgress(
    val seasonId: String,
    val seasonTitle: String,
    val weekIndex: Int,
    val weekTheme: String,
    val isFinaleWeek: Boolean,
    val chapterLine: String,
)
