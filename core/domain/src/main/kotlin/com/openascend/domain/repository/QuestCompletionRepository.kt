package com.openascend.domain.repository

import kotlinx.coroutines.flow.Flow

interface QuestCompletionRepository {
    fun observeCompletedIds(epochDay: Long): Flow<Set<String>>
    suspend fun completedIds(epochDay: Long): Set<String>
    suspend fun markComplete(questId: String, epochDay: Long)
}
