package com.openascend.data.export

import com.openascend.data.local.db.OpenAscendDatabase
import com.openascend.data.local.mapper.toDomain
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import javax.inject.Inject

/**
 * Local JSON export for privacy / portability. Not a sync protocol.
 */
class UserDataExporter @Inject constructor(
    private val database: OpenAscendDatabase,
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun buildJson(): String {
        val profile = database.profileDao().getProfile()?.toDomain()
        val habits = database.habitDao().snapshot().map { it.toDomain() }
        val today = LocalDate.now().toEpochDay()
        val start = today - 90L
        val metrics = database.dailyMetricDao().range(start, today).map { it.toDomain() }
        val xpTotal = database.xpDao().totalXp()
        val xpEvents = database.xpDao().snapshot(500).map { it.toDomain() }
        val payload = OpenAscendExport(
            exportedAtMillis = System.currentTimeMillis(),
            profile = profile,
            habits = habits,
            dailyMetrics = metrics,
            xpTotal = xpTotal,
            xpEvents = xpEvents,
        )
        return json.encodeToString(OpenAscendExport.serializer(), payload)
    }
}
