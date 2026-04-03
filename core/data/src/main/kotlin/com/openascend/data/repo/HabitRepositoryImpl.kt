package com.openascend.data.repo

import com.openascend.data.local.db.HabitDao
import com.openascend.data.local.db.HabitCompletionDao
import com.openascend.data.local.db.HabitCompletionEntity
import com.openascend.data.local.mapper.toDomain
import com.openascend.data.local.mapper.toEntity
import com.openascend.domain.model.Habit
import com.openascend.domain.model.HabitDraft
import com.openascend.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao,
) : HabitRepository {

    override fun observeHabits(): Flow<List<Habit>> =
        habitDao.observeHabits().map { list -> list.map { it.toDomain() } }

    override fun observeCompletionsForDay(epochDay: Long): Flow<Map<Long, Boolean>> =
        completionDao.observeForDay(epochDay).map { rows ->
            rows.associate { it.habitId to it.completed }
        }

    override suspend fun getHabit(id: Long): Habit? =
        habitDao.getById(id)?.toDomain()

    override suspend fun createHabit(draft: HabitDraft): Long {
        val entity = Habit(
            id = 0L,
            name = draft.name,
            frequencyPerWeek = draft.frequencyPerWeek,
            difficulty = draft.difficulty,
            linkedStat = draft.linkedStat,
            isRestDay = draft.isRestDay,
        ).toEntity()
        return habitDao.insert(entity)
    }

    override suspend fun updateHabit(habit: Habit) {
        habitDao.update(habit.toEntity())
    }

    override suspend fun deleteHabit(id: Long) {
        habitDao.getById(id)?.let { habitDao.delete(it) }
    }

    override suspend fun setCompleted(habitId: Long, epochDay: Long, completed: Boolean) {
        completionDao.upsert(
            HabitCompletionEntity(
                habitId = habitId,
                epochDay = epochDay,
                completed = completed,
            ),
        )
    }

    override suspend fun isCompleted(habitId: Long, epochDay: Long): Boolean =
        completionDao.isCompleted(habitId, epochDay) == true
}
