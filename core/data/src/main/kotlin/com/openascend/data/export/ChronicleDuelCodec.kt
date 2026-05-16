package com.openascend.data.export

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ChronicleDuelCodec {
    private val json = Json { ignoreUnknownKeys = true }

    fun encode(summary: ChronicleDuelSummary): String =
        json.encodeToString(ChronicleDuelSummary.serializer(), summary)

    fun decode(text: String): ChronicleDuelSummary? =
        runCatching {
            json.decodeFromString(ChronicleDuelSummary.serializer(), text)
        }.getOrNull()
}
