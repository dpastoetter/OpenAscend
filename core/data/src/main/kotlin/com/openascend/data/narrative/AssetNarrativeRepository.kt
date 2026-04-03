package com.openascend.data.narrative

import android.content.Context
import com.openascend.domain.narrative.NarrativePack
import com.openascend.domain.narrative.NarrativeRepository
import kotlinx.serialization.json.Json
import java.nio.charset.Charset

class AssetNarrativeRepository(
    private val context: Context,
) : NarrativeRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun loadPack(packId: String): NarrativePack {
        val id = packId.ifBlank { "default" }
        val path = "narrative/$id.json"
        return runCatching {
            context.assets.open(path).use { stream ->
                val text = stream.readBytes().toString(Charset.defaultCharset())
                val dto = json.decodeFromString<NarrativePackDto>(text)
                NarrativePack(
                    id = dto.id,
                    actTitles = dto.actTitles,
                    questActPrefix = dto.questActPrefix,
                    bossTellTemplates = dto.bossTellTemplates.ifEmpty {
                        listOf("{boss} stirs when {stat} runs thin.")
                    },
                    questTitleFlavorSuffixes = dto.questTitleFlavorSuffixes.ifEmpty { listOf("") },
                )
            }
        }.getOrElse { NarrativePack.fallback(id) }
    }
}
