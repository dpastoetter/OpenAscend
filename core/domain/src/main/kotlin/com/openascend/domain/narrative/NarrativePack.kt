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
    /** Optional per-stat boss tell lines (keys: RECOVERY, STAMINA, …). */
    val statBossTellTemplates: Map<String, List<String>> = emptyMap(),
    val statQuestChains: Map<String, StatChainFlavor> = emptyMap(),
    val seasons: List<NarrativeSeason> = emptyList(),
    val treasuryPrompts: TreasuryPrompts = TreasuryPrompts(),
) {
    companion object {
        fun fallback(id: String = "default") = NarrativePack(
            id = id,
            actTitles = listOf("The Unwritten Act"),
            questActPrefix = "",
            bossTellTemplates = listOf("{boss} stirs when {stat} runs thin."),
            questTitleFlavorSuffixes = listOf(""),
            statBossTellTemplates = emptyMap(),
            statQuestChains = emptyMap(),
            seasons = emptyList(),
            treasuryPrompts = TreasuryPrompts(),
        )
    }
}

data class TreasuryPrompts(
    val intro: String = "Close the treasury ledger—one honest beat, not financial advice.",
    val winLabel: String = "A win to inscribe",
    val leakLabel: String = "A leak to name",
    val intentionLabel: String = "Tomorrow's intention",
)
