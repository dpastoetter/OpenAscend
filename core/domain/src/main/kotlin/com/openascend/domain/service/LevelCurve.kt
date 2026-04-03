package com.openascend.domain.service

object LevelCurve {
    fun xpRequiredForLevel(level: Int): Int {
        if (level < 1) return 0
        // Gentle RPG curve: base + quadratic bump
        return 120 + (level - 1) * (40 + (level - 1) * 8)
    }

    fun levelFromTotalXp(totalXp: Int): Pair<Int, Int> {
        var level = 1
        var remaining = totalXp
        while (true) {
            val need = xpRequiredForLevel(level)
            if (remaining < need) return level to remaining
            remaining -= need
            level++
        }
    }

    fun xpToNextLevel(level: Int, xpInLevel: Int): Int =
        (xpRequiredForLevel(level) - xpInLevel).coerceAtLeast(0)
}
