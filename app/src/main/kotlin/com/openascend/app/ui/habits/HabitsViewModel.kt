package com.openascend.app.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openascend.domain.model.CoreStat
import com.openascend.domain.model.Habit
import com.openascend.domain.model.HabitDraft
import com.openascend.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HabitsUiState(
    val habits: List<Habit>,
)

@HiltViewModel
class HabitsViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
) : ViewModel() {

    val uiState: StateFlow<HabitsUiState> = habitRepository.observeHabits()
        .map { HabitsUiState(habits = it) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            HabitsUiState(emptyList()),
        )

    fun addHabit(name: String, perWeek: Int, difficulty: Int, stat: CoreStat, isRestDay: Boolean = false) {
        viewModelScope.launch {
            habitRepository.createHabit(
                HabitDraft(
                    name = name,
                    frequencyPerWeek = perWeek.coerceIn(1, 7),
                    difficulty = difficulty.coerceIn(1, 5),
                    linkedStat = stat,
                    isRestDay = isRestDay,
                ),
            )
        }
    }

    fun deleteHabit(id: Long) {
        viewModelScope.launch { habitRepository.deleteHabit(id) }
    }
}
