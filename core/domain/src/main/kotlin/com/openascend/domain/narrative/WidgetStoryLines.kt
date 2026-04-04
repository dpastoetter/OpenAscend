package com.openascend.domain.narrative

/** Short flavor lines for the home widget; rotates by day so the widget feels like a story beat. */
object WidgetStoryLines {

    private val lines: List<String> = listOf(
        "The chronicle ticks—one honest seal at a time.",
        "Your familiar stirs when habits and quests move.",
        "Boss weeks are chapters, not verdicts.",
        "Return for the evening seal; the tale likes closure.",
        "Stats are metaphors—kindness to yourself is canon.",
    )

    fun pick(epochDay: Long, flavorSalt: String): String {
        val idx = ((epochDay * 31L) + flavorSalt.hashCode()).mod(lines.size)
        return lines[idx]
    }
}
