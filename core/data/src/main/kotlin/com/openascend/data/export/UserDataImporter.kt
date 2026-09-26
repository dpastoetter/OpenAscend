package com.openascend.data.export

import androidx.room.withTransaction
import com.openascend.data.local.db.HabitCompletionEntity
import com.openascend.data.local.db.OpenAscendDatabase
import com.openascend.data.local.db.QuestCompletionEntity
import com.openascend.data.local.mapper.toEntity
import com.openascend.data.local.prefs.PrivacyPreferences
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataImporter @Inject constructor(
    private val database: OpenAscendDatabase,
    private val privacyPreferences: PrivacyPreferences,
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun importFromJsonString(text: String): Result<Unit> = runCatching {
        require(text.length <= MAX_BACKUP_CHARS) { "Backup file too large" }
        val export = json.decodeFromString(OpenAscendExport.serializer(), text)
        validateExport(export)
        database.withTransaction {
            database.habitCompletionDao().deleteAll()
            database.questCompletionDao().deleteAll()
            database.xpDao().deleteAll()
            database.dailyMetricDao().deleteAll()
            database.habitDao().deleteAll()
            database.profileDao().deleteAll()

            export.profile?.toEntity()?.let { database.profileDao().upsert(it) }
            if (export.habits.isNotEmpty()) {
                database.habitDao().upsertAll(export.habits.map { it.toEntity() })
            }
            export.dailyMetrics.forEach { database.dailyMetricDao().upsert(it.toEntity()) }
            export.xpEvents.forEach { database.xpDao().upsert(it.toEntity()) }
            if (export.schemaVersion >= 2) {
                export.habitCompletions.forEach { row ->
                    database.habitCompletionDao().upsert(
                        HabitCompletionEntity(
                            habitId = row.habitId,
                            epochDay = row.epochDay,
                            completed = row.completed,
                        ),
                    )
                }
                export.questCompletions.forEach { row ->
                    database.questCompletionDao().insert(
                        QuestCompletionEntity(
                            questId = row.questId,
                            epochDay = row.epochDay,
                        ),
                    )
                }
            }
        }
        export.privacy?.let { privacyPreferences.save(it) }
    }

    companion object {
        private const val MAX_BACKUP_CHARS = 32 * 1024 * 1024
        private const val MAX_LIST_ITEMS = 50_000
        private val SUPPORTED_SCHEMA_VERSIONS = 1..2

        internal fun validateExport(export: OpenAscendExport) {
            require(export.schemaVersion in SUPPORTED_SCHEMA_VERSIONS) {
                "Unsupported backup schemaVersion ${export.schemaVersion}"
            }
            require(export.exportedAtMillis > 0L) { "Backup missing exportedAtMillis" }
            require(export.profile != null) { "Backup missing profile" }
            require(export.habits.size <= MAX_LIST_ITEMS) { "Backup habits list too large" }
            require(export.dailyMetrics.size <= MAX_LIST_ITEMS) { "Backup dailyMetrics list too large" }
            require(export.xpEvents.size <= MAX_LIST_ITEMS) { "Backup xpEvents list too large" }
            require(export.habitCompletions.size <= MAX_LIST_ITEMS) { "Backup habitCompletions list too large" }
            require(export.questCompletions.size <= MAX_LIST_ITEMS) { "Backup questCompletions list too large" }
        }
    }
}
