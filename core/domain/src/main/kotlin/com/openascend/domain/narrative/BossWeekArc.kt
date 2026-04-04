package com.openascend.domain.narrative

import com.openascend.domain.model.CoreStat
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Short home-screen beats for the weekly boss cycle (calendar week, Mon–Sun).
 */
object BossWeekArc {

    fun homeBannerLine(
        today: LocalDate,
        bossTargetStat: CoreStat,
        bossDeferredThisWeek: Boolean,
    ): String? {
        if (bossDeferredThisWeek) {
            return when (today.dayOfWeek) {
                DayOfWeek.SATURDAY, DayOfWeek.SUNDAY ->
                    "Boss deferred—soft chapter this week. Clear the flag in weekly review when you want the tale sharp again."
                else -> null
            }
        }
        return when (today.dayOfWeek) {
            DayOfWeek.MONDAY ->
                "New boss week: ${bossTargetStat.name.lowercase()} is the crack in your armor—one small prep act today counts double in the story."
            DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY ->
                "Mid-week nudge: feed ${bossTargetStat.name.lowercase()} a honest rep—walk, log, stretch, or seal a habit."
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY ->
                "The encounter looms—pick one move that shores up ${bossTargetStat.name.lowercase()} before the scroll turns."
            DayOfWeek.SATURDAY, DayOfWeek.SUNDAY ->
                "Weekend: face the boss ritual when ready, or defer from weekly review for a gentler chapter."
            else -> null
        }
    }
}
