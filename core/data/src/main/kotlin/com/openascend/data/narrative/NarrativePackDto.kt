package com.openascend.data.narrative

import kotlinx.serialization.Serializable

@Serializable
internal data class NarrativePackDto(
    val id: String,
    val actTitles: List<String> = emptyList(),
    val questActPrefix: String = "",
    val bossTellTemplates: List<String> = emptyList(),
    val questTitleFlavorSuffixes: List<String> = emptyList(),
)
