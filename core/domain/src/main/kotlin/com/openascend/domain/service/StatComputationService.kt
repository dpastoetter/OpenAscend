package com.openascend.domain.service

import com.openascend.domain.model.DailyMetric
import com.openascend.domain.model.Habit
import com.openascend.domain.model.StatBlock
import kotlin.math.roundToInt

/**
 * Maps manual inputs into 0–100 style stats. Heuristic only — not medical or financial advice.
 */
class StatComputationService {

    fun computeToday(
        today: DailyMetric?,
        habits: List<Habit>,
        habitCompletionsToday: Map<Long, Boolean>,
    ): StatBlock {
        val recovery = scoreRecovery(today?.sleepHours)
        val stamina = scoreStamina(today?.steps)
        val stability = scoreStability(today?.bankControlScore)
        val discipline = scoreDisciplineForDay(habits, habitCompletionsToday)
        val vitality = scoreVitality(today?.vitalityScore)

        return StatBlock(
            recovery = recovery,
            stamina = stamina,
            stability = stability,
            discipline = discipline,
            vitality = vitality,
        )
    }

    fun computeRollingSevenDay(
        lastSevenDays: List<DailyMetric>,
        habits: List<Habit>,
        isHabitCompleted: (habitId: Long, epochDay: Long) -> Boolean,
        todayEpochDay: Long,
    ): StatBlock {
        if (lastSevenDays.isEmpty() && habits.isEmpty()) {
            return StatBlock(50, 50, 50, 50, 50)
        }
        val recoveryAvg = averageInt(lastSevenDays.map { scoreRecovery(it.sleepHours) })
        val staminaAvg = averageInt(lastSevenDays.map { scoreStamina(it.steps) })
        val stabilityAvg = averageInt(lastSevenDays.map { scoreStability(it.bankControlScore) })
        val vitalityAvg = averageInt(lastSevenDays.map { scoreVitality(it.vitalityScore) })

        val disciplineScores = (0L..6L).map { offset ->
            val day = todayEpochDay - offset
            val map = habits.associate { h -> h.id to isHabitCompleted(h.id, day) }
            scoreDisciplineForDay(habits, map)
        }
        val disciplineAvg = averageInt(disciplineScores)

        return StatBlock(
            recovery = recoveryAvg,
            stamina = staminaAvg,
            stability = stabilityAvg,
            discipline = disciplineAvg,
            vitality = vitalityAvg,
        )
    }

    private fun averageInt(values: List<Int>): Int {
        if (values.isEmpty()) return 50
        return (values.sum().toDouble() / values.size).roundToInt().coerceIn(0, 100)
    }

    private fun scoreRecovery(sleepHours: Float?): Int {
        if (sleepHours == null) return 45
        val h = sleepHours.toDouble()
        val score = when {
            h < 5 -> 30
            h < 6.5 -> 55
            h < 8 -> 85
            h < 9.5 -> 75
            else -> 60
        }
        return score.coerceIn(0, 100)
    }

    private fun scoreStamina(steps: Int?): Int {
        if (steps == null) return 45
        return when {
            steps < 2000 -> 35
            steps < 5000 -> 55
            steps < 8000 -> 75
            steps < 12000 -> 90
            else -> 95
        }.coerceIn(0, 100)
    }

    private fun scoreStability(bankControl: Int?): Int {
        if (bankControl == null) return 45
        return (bankControl * 10).coerceIn(0, 100)
    }

    private fun scoreVitality(v: Int?): Int {
        if (v == null) return 40
        return (v * 10).coerceIn(0, 100)
    }

    private fun scoreDisciplineForDay(
        habits: List<Habit>,
        habitCompletions: Map<Long, Boolean>,
    ): Int {
        if (habits.isEmpty()) return 50
        var done = 0
        for (h in habits) {
            if (habitCompletions[h.id] == true) done++
        }
        return ((done.toDouble() / habits.size) * 100).roundToInt().coerceIn(0, 100)
    }
}
