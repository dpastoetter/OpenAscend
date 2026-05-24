package com.openascend.domain.narrative

import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

object SeasonProgressResolver {

    private const val WEEKS_PER_SEASON = 4

    fun resolve(todayEpochDay: Long, pack: NarrativePack): SeasonProgress? {
        if (pack.seasons.isEmpty()) return null
        val date = LocalDate.ofEpochDay(todayEpochDay)
        val weekFields = WeekFields.of(Locale.getDefault())
        val weekOfYear = date.get(weekFields.weekOfWeekBasedYear())
        val seasonIndex = ((weekOfYear - 1) / WEEKS_PER_SEASON) % pack.seasons.size.coerceAtLeast(1)
        val active = pack.seasons[seasonIndex]
        val weekInSeason = ((weekOfYear - 1) % WEEKS_PER_SEASON) + 1
        val theme = active.weekThemes.getOrElse(weekInSeason - 1) { active.weekThemes.lastOrNull() ?: "" }
        val finale = weekInSeason >= WEEKS_PER_SEASON
        val chapter = if (finale) {
            "Finale week · ${active.title} — face ${active.finaleBossName}"
        } else {
            "Week $weekInSeason of ${active.title} — $theme"
        }
        return SeasonProgress(
            seasonId = active.id,
            seasonTitle = active.title,
            weekIndex = weekInSeason,
            weekTheme = theme,
            isFinaleWeek = finale,
            chapterLine = chapter,
        )
    }
}
