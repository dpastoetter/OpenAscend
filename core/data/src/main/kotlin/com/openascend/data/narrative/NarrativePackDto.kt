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
)

@Serializable
internal data class StatChainFlavorDto(
    val chainTitle: String,
    val chainDescription: String,
)
