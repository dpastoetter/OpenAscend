package com.openascend.domain.narrative

/**
 * Maps saved evening mood ids to a one-line home headline shown the next calendar day.
 */
object EveningMoodCopy {
    private val map = mapOf(
        "steady" to "Yesterday you named the day steady—carry that tempo gently.",
        "scattered" to "Yesterday felt scattered; today favors one anchor habit.",
        "heavy" to "Yesterday sat heavy—a softer target still counts.",
        "bright" to "Yesterday had a bright edge; ride that lift without forcing more.",
    )

    fun headlineForYesterday(moodIdsCsv: String?): String? {
        if (moodIdsCsv.isNullOrBlank()) return null
        val first = moodIdsCsv.split(',').firstOrNull()?.trim().orEmpty()
        return map[first] ?: "Yesterday's mood still echoes—pick one kind win."
    }
}
