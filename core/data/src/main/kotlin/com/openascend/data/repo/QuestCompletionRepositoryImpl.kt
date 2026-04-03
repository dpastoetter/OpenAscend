package com.openascend.data.repo

import com.openascend.data.local.db.QuestCompletionDao
import com.openascend.data.local.db.QuestCompletionEntity
import com.openascend.domain.repository.QuestCompletionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class QuestCompletionRepositoryImpl @Inject constructor(
    private val dao: QuestCompletionDao,
) : QuestCompletionRepository {

    override fun observeCompletedIds(epochDay: Long): Flow<Set<String>> =
        dao.observeForDay(epochDay).map { it.toSet() }

    override suspend fun completedIds(epochDay: Long): Set<String> =
        dao.idsForDay(epochDay).toSet()

    override suspend fun markComplete(questId: String, epochDay: Long) {
        dao.insert(QuestCompletionEntity(questId = questId, epochDay = epochDay))
    }
}
