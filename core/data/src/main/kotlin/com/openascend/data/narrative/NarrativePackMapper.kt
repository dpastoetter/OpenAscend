package com.openascend.data.narrative

import com.openascend.domain.narrative.NarrativePack
import com.openascend.domain.narrative.NarrativeSeason
import com.openascend.domain.narrative.StatChainFlavor
import com.openascend.domain.narrative.TreasuryPrompts

internal object NarrativePackMapper {
    fun fromDto(dto: NarrativePackDto): NarrativePack = NarrativePack(
        id = dto.id,
        actTitles = dto.actTitles,
        questActPrefix = dto.questActPrefix,
        bossTellTemplates = dto.bossTellTemplates.ifEmpty {
            listOf("{boss} stirs when {stat} runs thin.")
        },
        questTitleFlavorSuffixes = dto.questTitleFlavorSuffixes.ifEmpty { listOf("") },
        statBossTellTemplates = dto.statBossTellTemplates,
        statQuestChains = dto.statQuestChains.mapValues { (_, v) ->
            StatChainFlavor(v.chainTitle, v.chainDescription)
        },
        seasons = dto.seasons.map {
            NarrativeSeason(
                id = it.id,
                title = it.title,
                weekThemes = it.weekThemes,
                finaleBossName = it.finaleBossName,
            )
        },
        treasuryPrompts = dto.treasuryPrompts?.let {
            TreasuryPrompts(
                intro = it.intro.ifBlank { TreasuryPrompts().intro },
                winLabel = it.winLabel.ifBlank { TreasuryPrompts().winLabel },
                leakLabel = it.leakLabel.ifBlank { TreasuryPrompts().leakLabel },
                intentionLabel = it.intentionLabel.ifBlank { TreasuryPrompts().intentionLabel },
            )
        } ?: TreasuryPrompts(),
    )
}
