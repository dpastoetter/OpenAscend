package com.openascend.domain.repository

import com.openascend.domain.model.XpEvent
import kotlinx.coroutines.flow.Flow

interface XpRepository {
    fun observeEvents(limit: Int = 100): Flow<List<XpEvent>>
    suspend fun appendEvent(amount: Int, reason: String)
    suspend fun totalXp(): Int
}
