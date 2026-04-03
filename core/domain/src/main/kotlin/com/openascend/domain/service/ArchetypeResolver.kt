package com.openascend.domain.service

import com.openascend.domain.model.CoreStat
import com.openascend.domain.model.PlayerArchetype
import com.openascend.domain.model.StatBlock

class ArchetypeResolver {
    fun resolve(stats: StatBlock): PlayerArchetype {
        val m = stats.asMap()
        val top = m.maxBy { it.value }
        val spread = m.values.max() - m.values.min()
        if (spread < 8) return PlayerArchetype.VAGABOND
        return when (top.key) {
            CoreStat.RECOVERY -> PlayerArchetype.SENTINEL
            CoreStat.STAMINA -> PlayerArchetype.STRIDER
            CoreStat.STABILITY -> PlayerArchetype.STEWARD
            CoreStat.DISCIPLINE -> PlayerArchetype.WARDEN
            CoreStat.VITALITY -> PlayerArchetype.ASCENDANT
        }
    }
}
