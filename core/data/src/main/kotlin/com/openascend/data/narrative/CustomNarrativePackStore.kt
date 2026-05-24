package com.openascend.data.narrative

import android.content.Context
import com.openascend.domain.narrative.NarrativePack
import kotlinx.serialization.json.Json
import java.io.File

/**
 * User-imported packs under filesDir/narrative/custom/{id}.json
 */
class CustomNarrativePackStore(
    private val context: Context,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val customDir: File
        get() = File(context.filesDir, "narrative/custom").also { it.mkdirs() }

    fun listCustomPackIds(): List<String> =
        customDir.listFiles()?.mapNotNull { file ->
            if (file.extension == "json") file.nameWithoutExtension else null
        }.orEmpty().sorted()

    fun loadPack(packId: String): NarrativePack? {
        val file = File(customDir, "$packId.json")
        if (!file.exists()) return null
        return runCatching {
            val dto = json.decodeFromString<NarrativePackDto>(file.readText())
            NarrativePackMapper.fromDto(dto)
        }.getOrNull()
    }

    fun importPack(fileName: String, jsonText: String): Result<String> = runCatching {
        val dto = json.decodeFromString<NarrativePackDto>(jsonText)
        require(dto.id.isNotBlank()) { "Pack id required" }
        require(dto.actTitles.isNotEmpty()) { "actTitles required" }
        val dest = File(customDir, "${dto.id}.json")
        dest.writeText(jsonText)
        dto.id
    }
}
