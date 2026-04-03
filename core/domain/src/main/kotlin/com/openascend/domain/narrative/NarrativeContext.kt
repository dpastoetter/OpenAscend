package com.openascend.domain.narrative

import java.time.LocalDate

/**
 * Calendar + pack slice used to flavor bosses and quests deterministically.
 */
data class NarrativeContext(
    val localDate: LocalDate,
    val pack: NarrativePack,
) {
    val actTitle: String get() = ActResolver.actTitleFor(localDate, pack)
}
