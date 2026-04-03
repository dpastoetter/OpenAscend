package com.openascend.domain.repository

import com.openascend.domain.model.Habit
import com.openascend.domain.model.HabitDraft
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun observeHabits(): Flow<List<Habit>>
    fun observeCompletionsForDay(epochDay: Long): Flow<Map<Long, Boolean>>
    suspend fun createHabit(draft: HabitDraft): Long
    suspend fun getHabit(id: Long): Habit?
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(id: Long)
    suspend fun setCompleted(habitId: Long, epochDay: Long, completed: Boolean)
    suspend fun isCompleted(habitId: Long, epochDay: Long): Boolean
}
