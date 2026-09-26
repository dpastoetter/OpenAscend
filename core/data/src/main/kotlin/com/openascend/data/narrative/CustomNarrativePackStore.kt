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
            if (file.extension != "json") return@mapNotNull null
            sanitizePackIdOrNull(file.nameWithoutExtension)
        }.orEmpty().sorted()

    fun loadPack(packId: String): NarrativePack? {
        val safeId = sanitizePackIdOrNull(packId) ?: return null
        val file = File(customDir, "$safeId.json")
        if (!isInsideCustomDir(file) || !file.exists()) return null
        return runCatching {
            val dto = json.decodeFromString<NarrativePackDto>(file.readText())
            NarrativePackMapper.fromDto(dto)
        }.getOrNull()
    }

    fun importPack(fileName: String, jsonText: String): Result<String> = runCatching {
        require(jsonText.length <= MAX_PACK_JSON_CHARS) { "Pack file too large" }
        val dto = json.decodeFromString<NarrativePackDto>(jsonText)
        val safeId = sanitizePackId(dto.id)
        require(dto.actTitles.isNotEmpty()) { "actTitles required" }
        val dest = File(customDir, "$safeId.json")
        check(isInsideCustomDir(dest)) { "Invalid pack destination" }
        dest.writeText(jsonText)
        safeId
    }

    private fun isInsideCustomDir(file: File): Boolean {
        val base = customDir.canonicalFile
        val candidate = file.canonicalFile
        return candidate.parentFile == base
    }

    companion object {
        private const val MAX_PACK_JSON_CHARS = 1_048_576

        private val PACK_ID_PATTERN = Regex("^[a-zA-Z0-9_-]{1,64}$")

        fun sanitizePackId(id: String): String {
            require(id.matches(PACK_ID_PATTERN)) { "Invalid pack id" }
            return id
        }

        fun sanitizePackIdOrNull(id: String): String? =
            id.takeIf { it.matches(PACK_ID_PATTERN) }
    }
}
