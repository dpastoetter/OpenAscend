package com.openascend.data.repo

import com.openascend.data.local.db.XpDao
import com.openascend.data.local.db.XpEventEntity
import com.openascend.data.local.mapper.toDomain
import com.openascend.domain.model.XpEvent
import com.openascend.domain.repository.XpRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class XpRepositoryImpl @Inject constructor(
    private val xpDao: XpDao,
) : XpRepository {

    override fun observeEvents(limit: Int): Flow<List<XpEvent>> =
        xpDao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun appendEvent(amount: Int, reason: String) {
        xpDao.insert(
            XpEventEntity(
                timestampMillis = System.currentTimeMillis(),
                amount = amount,
                reason = reason,
            ),
        )
    }

    override suspend fun totalXp(): Int = xpDao.totalXp()
}
