package com.openascend.domain.service

import com.openascend.domain.model.XpEvent
import com.openascend.domain.repository.XpRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeXpRepository : XpRepository {

    private var nextId = 1L
    private val events = MutableStateFlow<List<XpEvent>>(emptyList())

    override fun observeEvents(limit: Int): Flow<List<XpEvent>> =
        events.map { list -> list.take(limit) }

    override suspend fun appendEvent(amount: Int, reason: String) {
        val ev = XpEvent(
            id = nextId++,
            timestampMillis = 0L,
            amount = amount,
            reason = reason,
        )
        events.value = listOf(ev) + events.value
    }

    override suspend fun totalXp(): Int = events.value.sumOf { it.amount }
}
