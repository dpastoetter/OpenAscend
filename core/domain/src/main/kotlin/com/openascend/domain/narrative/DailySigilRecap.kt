package com.openascend.domain.narrative

/** Plain-text “daily sigil” for optional user-initiated sharing (offline). */
object DailySigilRecap {

    fun build(
        displayName: String,
        level: Int,
        actTitle: String,
        questsSealed: Int,
        questsTotal: Int,
        moodHeadlineYesterday: String?,
        archetypeLine: String,
    ): String {
        val mood = moodHeadlineYesterday?.let { "Yesterday's margin: $it" } ?: "Yesterday's margin: (open)"
        val seal = if (questsTotal > 0) "$questsSealed / $questsTotal daily quests sealed today" else "No daily quests on the scroll today"
        return buildString {
            appendLine("OpenAscend · daily sigil")
            appendLine("$displayName · Lv $level · $archetypeLine")
            appendLine("Act: $actTitle")
            appendLine(mood)
            appendLine(seal)
            appendLine("— user-generated recap, not medical or financial advice.")
        }.trim()
    }
}
