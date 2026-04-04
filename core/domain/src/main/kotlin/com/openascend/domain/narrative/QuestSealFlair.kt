package com.openascend.domain.narrative

import com.openascend.domain.model.CoreStat

/** One-line celebration when a daily quest is sealed. */
object QuestSealFlair {
    fun line(stat: CoreStat): String = when (stat) {
        CoreStat.RECOVERY -> "Sealed—your chronicle breathes easier."
        CoreStat.STAMINA -> "Sealed—the trail remembers your steps."
        CoreStat.STABILITY -> "Sealed—the ledger feels steadier."
        CoreStat.DISCIPLINE -> "Sealed—oath kept, XP earned."
        CoreStat.VITALITY -> "Sealed—vitality noted in the margins."
    }
}
