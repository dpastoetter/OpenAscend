package com.openascend.domain.service

import com.openascend.domain.model.GameQuest
import com.openascend.domain.model.Habit
import java.time.DayOfWeek
import java.time.LocalDate

enum class ChronicleCompassKind {
    WeeklyReview,
    EveningCheckIn,
    BossEncounter,
    SealQuest,
    Steady,
}

data class ChronicleCompassDirective(
    val kind: ChronicleCompassKind,
    val questTitle: String? = null,
    val bossName: String? = null,
    val habitsOpenCount: Int = 0,
)

/**
 * Picks one primary Home CTA based on time of day and chronicle state.
 */
object ChronicleCompassResolver {

    fun resolve(
        todayEpochDay: Long,
        hourOfDay: Int,
        loggedToday: Boolean,
        openQuest: GameQuest?,
        bossSealedThisWeek: Boolean,
        bossName: String,
        habits: List<Habit>,
        todayCompletions: Map<Long, Boolean>,
    ): ChronicleCompassDirective {
        val dayOfWeek = LocalDate.ofEpochDay(todayEpochDay).dayOfWeek
        val activeHabits = habits.filter { !it.isRestDay }
        val habitsOpenCount = activeHabits.count { todayCompletions[it.id] != true }

        if (dayOfWeek == DayOfWeek.SUNDAY) {
            return ChronicleCompassDirective(ChronicleCompassKind.WeeklyReview)
        }
        if (!loggedToday && hourOfDay >= 17) {
            return ChronicleCompassDirective(
                kind = ChronicleCompassKind.EveningCheckIn,
                habitsOpenCount = habitsOpenCount,
            )
        }
        if (!bossSealedThisWeek) {
            return ChronicleCompassDirective(
                kind = ChronicleCompassKind.BossEncounter,
                bossName = bossName,
            )
        }
        val quest = openQuest
        if (quest != null) {
            return ChronicleCompassDirective(
                kind = ChronicleCompassKind.SealQuest,
                questTitle = quest.title,
            )
        }
        if (!loggedToday) {
            return ChronicleCompassDirective(
                kind = ChronicleCompassKind.EveningCheckIn,
                habitsOpenCount = habitsOpenCount,
            )
        }
        return ChronicleCompassDirective(
            kind = ChronicleCompassKind.Steady,
            habitsOpenCount = habitsOpenCount,
        )
    }
}
