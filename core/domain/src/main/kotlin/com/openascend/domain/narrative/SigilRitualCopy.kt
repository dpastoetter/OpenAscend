package com.openascend.domain.narrative

/**
 * Copy for the optional “Seal the sigil” micro-ritual after evening check-in.
 * Framed as chronicle theater only—no health, therapy, or financial claims.
 */
object SigilRitualCopy {
    const val title: String = "Seal the sigil"
    const val subtitle: String =
        "Optional flourish: tap the three seals in order. Pure chronicle theater—not therapy or medical advice."
    const val orderHint: String = "Tap in order: 1 → 2 → 3"
    val runeLabels: List<String> = listOf("Dawn seal", "Path seal", "Rest seal")
    const val skipLabel: String = "Skip ritual"
    const val skipContentDescription: String = "Skip and go back"
    const val resetHint: String = "Order broken—begin the trace again."
    const val wrongTitle: String = "Trace broken"
    const val wrongOk: String = "OK"
    const val successDialogTitle: String = "Sigil sealed"
    const val disclaimerLine: String =
        "Theater for your chronicle only—not therapy, medical, or financial advice."
    const val returnHomeLabel: String = "Return home"
    const val runeGlyph: String = "ᚠ"
    const val nextMarker: String = "  ← next"
    const val footerNote: String =
        "Money, sleep, and mood entries you sealed earlier are still just a mirror—not professional guidance."
    fun successLines(): List<String> = listOf(
        "The sigil holds—your chronicle likes a little theater.",
        "Three seals, one quiet line in the margin.",
        "The realm notes the gesture; nothing more is promised.",
    )
}
