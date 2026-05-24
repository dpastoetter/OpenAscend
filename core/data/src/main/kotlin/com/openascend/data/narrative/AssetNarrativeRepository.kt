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

    private val customStore = CustomNarrativePackStore(context)

    override suspend fun loadPack(packId: String): NarrativePack {
        val id = packId.ifBlank { "default" }
        customStore.loadPack(id)?.let { return it }
        val path = "narrative/$id.json"
        return runCatching {
            context.assets.open(path).use { stream ->
                val text = stream.readBytes().toString(Charset.defaultCharset())
                val dto = json.decodeFromString<NarrativePackDto>(text)
                NarrativePackMapper.fromDto(dto)
            }
        }.getOrElse { NarrativePack.fallback(id) }
    }

    fun listAvailablePackIds(): List<String> {
        val bundled = context.assets.list("narrative")?.mapNotNull { name ->
            if (name.endsWith(".json") && !name.startsWith("examples/")) {
                name.removeSuffix(".json")
            } else {
                null
            }
        }.orEmpty()
        return (bundled + customStore.listCustomPackIds()).distinct().sorted()
    }

    fun customPackStore(): CustomNarrativePackStore = customStore
}
