package com.openascend.domain.narrative

/**
 * Stable compliment for share / level-up sheet (deterministic from level + name).
 */
object LevelUpFlair {
    private val lines = listOf(
        "The chronicle notes your step forward—steady beats flashy.",
        "New level: the realm leans in, curious what you will do with it.",
        "Your legend gains a footnote; small ink, real weight.",
        "The path widens one notch—enough for kinder habits.",
        "XP well spent: the world feels a shade more yours.",
    )

    fun compliment(level: Int, displayName: String): String {
        val seed = (level * 1315423911L) xor displayName.hashCode().toLong()
        val idx = kotlin.math.abs(seed % lines.size).toInt()
        return lines[idx]
    }
}
