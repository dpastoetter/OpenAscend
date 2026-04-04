package com.openascend.domain.narrative

import java.time.LocalDate

/**
 * Deterministic act title from calendar month (0–11) and narrative pack.
 */
object ActResolver {
    fun monthIndex(date: LocalDate): Int = date.monthValue - 1

    fun actTitleFor(date: LocalDate, pack: NarrativePack): String {
        val titles = pack.actTitles
        if (titles.isEmpty()) return ""
        return titles[monthIndex(date) % titles.size]
    }

    /** Inclusive days from [date] through end of calendar month (this narrative "act"). */
    fun daysRemainingInAct(date: LocalDate): Int =
        date.lengthOfMonth() - date.dayOfMonth + 1
}
