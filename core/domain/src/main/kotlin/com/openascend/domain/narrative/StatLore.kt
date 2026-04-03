package com.openascend.domain.narrative

import com.openascend.domain.model.CoreStat

object StatLore {
    fun line(stat: CoreStat): String = when (stat) {
        CoreStat.RECOVERY -> "Recovery is rest, wind-down, and honest sleep—your mana bar for everything else."
        CoreStat.STAMINA -> "Stamina is motion and breath—steps, breaks, and showing up in the body."
        CoreStat.STABILITY -> "Stability is how money and logistics feel under your hand—not perfection, clarity."
        CoreStat.DISCIPLINE -> "Discipline is the small promise kept; momentum hides inside tiny reps."
        CoreStat.VITALITY -> "Vitality is the long arc—hydration, gentle upkeep, and future-you kindness."
    }
}
