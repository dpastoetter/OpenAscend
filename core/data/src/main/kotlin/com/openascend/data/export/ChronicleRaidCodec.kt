package com.openascend.data.export

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ChronicleRaidCodec {
    private val json = Json { ignoreUnknownKeys = true }

    fun encodeMember(summary: ChronicleDuelSummary): String =
        json.encodeToString(ChronicleDuelSummary.serializer(), summary)

    fun decodeMember(text: String): ChronicleDuelSummary? =
        runCatching {
            json.decodeFromString(ChronicleDuelSummary.serializer(), text)
        }.getOrNull()
}
