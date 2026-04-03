package com.openascend.domain.narrative

/**
 * Benign morning omens; habit name interpolated for personalization.
 */
object OmenPhrases {
    private val templates = listOf(
        "A soft omen: {habit} wants a honest try before noon.",
        "The wind favors {habit}—one small rep opens the day.",
        "Constellation hint: {habit} glows faintly on today's chart.",
    )

    fun pick(seed: Long, habitName: String): String {
        val h = habitName.ifBlank { "your next habit" }
        val t = templates[(kotlin.math.abs(seed) % templates.size).toInt()]
        return t.replace("{habit}", h)
    }
}
