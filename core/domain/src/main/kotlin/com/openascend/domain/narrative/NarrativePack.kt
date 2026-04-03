package com.openascend.domain.narrative

/**
 * Loaded from assets/narrative/{id}.json — contributor-editable flavor.
 */
data class NarrativePack(
    val id: String,
    val actTitles: List<String>,
    /** Prepended to quest titles when non-blank (e.g. seasonal framing). */
    val questActPrefix: String,
    val bossTellTemplates: List<String>,
    val questTitleFlavorSuffixes: List<String>,
) {
    companion object {
        fun fallback(id: String = "default") = NarrativePack(
            id = id,
            actTitles = listOf("The Unwritten Act"),
            questActPrefix = "",
            bossTellTemplates = listOf("{boss} stirs when {stat} runs thin."),
            questTitleFlavorSuffixes = listOf(""),
        )
    }
}
