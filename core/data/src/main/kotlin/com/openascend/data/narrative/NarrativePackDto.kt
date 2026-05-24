package com.openascend.data.narrative

import kotlinx.serialization.Serializable

@Serializable
internal data class NarrativePackDto(
    val id: String,
    val actTitles: List<String> = emptyList(),
    val questActPrefix: String = "",
    val bossTellTemplates: List<String> = emptyList(),
    val questTitleFlavorSuffixes: List<String> = emptyList(),
    val statBossTellTemplates: Map<String, List<String>> = emptyMap(),
    val statQuestChains: Map<String, StatChainFlavorDto> = emptyMap(),
    val seasons: List<NarrativeSeasonDto> = emptyList(),
    val treasuryPrompts: TreasuryPromptsDto? = null,
)

@Serializable
internal data class NarrativeSeasonDto(
    val id: String,
    val title: String,
    val weekThemes: List<String> = emptyList(),
    val finaleBossName: String = "",
)

@Serializable
internal data class TreasuryPromptsDto(
    val intro: String = "",
    val winLabel: String = "",
    val leakLabel: String = "",
    val intentionLabel: String = "",
)

@Serializable
internal data class StatChainFlavorDto(
    val chainTitle: String,
    val chainDescription: String,
)
